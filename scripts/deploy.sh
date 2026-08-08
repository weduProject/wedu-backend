#!/bin/bash

APP_DIR=$(dirname "$(readlink -f "$0")")
APP_NAME="wedu-backend"
SERVICE_NAME="wedu"
BASE_ENV_FILE="/etc/wedu/wedu.env"
GITHUB_ENV_FILE="/etc/wedu/wedu-github.env"
JAR_FILE=$(ls -t "$APP_DIR"/*.jar 2>/dev/null | head -1)
LOG_FILE="$APP_DIR/app.log"

if [ -z "$JAR_FILE" ]; then
  echo "JAR 파일을 찾을 수 없습니다."
  exit 1
fi

echo "=== $APP_NAME 배포 시작 ==="
echo "JAR: $JAR_FILE"

if [ ! -f "$BASE_ENV_FILE" ]; then
  echo "환경변수 파일을 찾을 수 없습니다: $BASE_ENV_FILE"
  echo "DB_URL, DB_USERNAME, DB_PASSWORD 등 서버별 비밀값을 먼저 설정하세요."
  exit 1
fi

echo "DB 연결 정보(호스트만, 자격증명 제외): $(grep -E '^DB_URL=' "$BASE_ENV_FILE" | cut -d= -f2-)"
echo "wedu.env 에 정의된 키 목록(값 제외): $(grep -oE '^[A-Z_]+=' "$BASE_ENV_FILE" | tr -d '=' | tr '\n' ' ')"
echo "로컬 3306 포트 리스닝 여부:"
(sudo ss -tlnp 2>/dev/null | grep ':3306' ) || echo "  (3306 리스닝 없음 — 로컬 MySQL 아님)"
echo "로컬 mysql/mariadb 서비스 상태:"
(systemctl is-active mysql 2>/dev/null; systemctl is-active mariadb 2>/dev/null; systemctl is-active mysqld 2>/dev/null) || true

echo "systemd 서비스 설정 갱신"
sudo tee "/etc/systemd/system/$SERVICE_NAME.service" > /dev/null <<EOF
[Unit]
Description=WEDU Backend
After=network.target

[Service]
User=$(whoami)
WorkingDirectory=$APP_DIR
EnvironmentFile=$BASE_ENV_FILE
EnvironmentFile=-$GITHUB_ENV_FILE
Environment="SERVER_FORWARD_HEADERS_STRATEGY=framework"
ExecStart=/usr/bin/java -Dspring.profiles.active=prod -Dfile.encoding=UTF-8 -jar $JAR_FILE
Restart=always
RestartSec=5
StandardOutput=append:$LOG_FILE
StandardError=append:$LOG_FILE

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable "$SERVICE_NAME"

echo "로그 로테이션 설정(7일 보관)"
sudo tee "/etc/logrotate.d/$SERVICE_NAME" > /dev/null <<EOF
$LOG_FILE {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    copytruncate
}
EOF

echo "DB 일일 백업 스케줄 등록"
sudo install -d -m 700 -o root -g root /var/backups/wedu
sudo tee /usr/local/bin/wedu-db-backup.sh > /dev/null <<'BACKUP_EOF'
#!/bin/bash
set -euo pipefail
umask 077
BASE_ENV_FILE="/etc/wedu/wedu.env"
BACKUP_DIR="/var/backups/wedu"
RETENTION_DAYS=7

DB_URL=$(grep -E '^DB_URL=' "$BASE_ENV_FILE" | cut -d= -f2-)
DB_USERNAME=$(grep -E '^DB_USERNAME=' "$BASE_ENV_FILE" | cut -d= -f2-)
DB_PASSWORD=$(grep -E '^DB_PASSWORD=' "$BASE_ENV_FILE" | cut -d= -f2-)
DB_HOST=$(echo "$DB_URL" | sed -E 's#.*://([^:/]+).*#\1#')
DB_NAME=$(echo "$DB_URL" | sed -E 's#.*/([A-Za-z0-9_]+)(\?.*)?$#\1#')

TIMESTAMP=$(date +%Y%m%d)
OUTPUT_FILE="$BACKUP_DIR/wedu-$TIMESTAMP.sql.gz"
TEMP_OUTPUT=$(mktemp "$BACKUP_DIR/.wedu-$TIMESTAMP.XXXXXX.sql.gz")
trap 'rm -f "$TEMP_OUTPUT"' EXIT

MYSQL_PWD="$DB_PASSWORD" mysqldump -h "$DB_HOST" -u "$DB_USERNAME" "$DB_NAME" | gzip > "$TEMP_OUTPUT"
mv "$TEMP_OUTPUT" "$OUTPUT_FILE"
trap - EXIT

find "$BACKUP_DIR" -name 'wedu-*.sql.gz' -mtime "+$RETENTION_DAYS" -delete
BACKUP_EOF
sudo chmod 700 /usr/local/bin/wedu-db-backup.sh

sudo tee /etc/cron.d/wedu-db-backup > /dev/null <<EOF
0 18 * * * root /usr/local/bin/wedu-db-backup.sh >> /var/log/wedu-db-backup.log 2>&1
EOF

echo "헬스체크 상시 감시(다운 시에만 Discord 알림) 등록"
sudo tee /usr/local/bin/wedu-health-alert.sh > /dev/null <<'HEALTH_EOF'
#!/bin/bash
GITHUB_ENV_FILE="/etc/wedu/wedu-github.env"
STATE_FILE="/var/run/wedu-health-down"
DISCORD_WEBHOOK_URL=$(grep -E '^DISCORD_WEBHOOK_URL=' "$GITHUB_ENV_FILE" 2>/dev/null | cut -d= -f2-)

HEALTH_BODY=$(curl --fail --silent --show-error --connect-timeout 2 --max-time 5 \
    http://localhost:8080/actuator/health 2>/dev/null)

if echo "$HEALTH_BODY" | grep -q '"status"[[:space:]]*:[[:space:]]*"UP"'; then
  rm -f "$STATE_FILE"
  exit 0
fi

if [ -f "$STATE_FILE" ]; then
  exit 0
fi

if [ -n "$DISCORD_WEBHOOK_URL" ]; then
  PAYLOAD='{"embeds":[{"title":"🚨 wedu-backend 헬스체크 실패","description":"/actuator/health 응답 없음 또는 UP 아님","color":15158332}]}'
  if curl -sf -X POST "$DISCORD_WEBHOOK_URL" -H "Content-Type: application/json" -d "$PAYLOAD"; then
    touch "$STATE_FILE"
  fi
fi
HEALTH_EOF
sudo chmod 700 /usr/local/bin/wedu-health-alert.sh

sudo tee /etc/cron.d/wedu-health-check > /dev/null <<EOF
*/5 * * * * root /usr/local/bin/wedu-health-alert.sh >> /var/log/wedu-health-check.log 2>&1
EOF

# 이전 nohup 배포 프로세스가 남아 있으면 8080 포트 충돌을 막기 위해 정리한다.
if ! sudo systemctl is-active --quiet "$SERVICE_NAME"; then
  PID=$(pgrep -f "$APP_NAME.*\.jar" || true)
  if [ -n "$PID" ]; then
    echo "기존 프로세스 종료 (PID: $PID)"
    kill -15 "$PID" || true
    sleep 5
    if kill -0 "$PID" 2>/dev/null; then
      kill -9 "$PID" || true
    fi
  fi
fi

sudo systemctl restart "$SERVICE_NAME"
echo "서비스 재시작 완료"

# 헬스체크 (최대 30초)
HEALTHY=0
for i in $(seq 1 30); do
  sleep 1
  HEALTH_BODY=$(curl --fail --silent --show-error \
      --connect-timeout 2 --max-time 5 \
      http://localhost:8080/actuator/health 2>/dev/null) || continue
  if echo "$HEALTH_BODY" | grep -q '"status"[[:space:]]*:[[:space:]]*"UP"'; then
    HEALTHY=1
    break
  fi
done

if [ "$HEALTHY" -ne 1 ]; then
  echo "헬스체크 실패 — 서비스 상태와 로그를 확인하세요."
  sudo systemctl status "$SERVICE_NAME" --no-pager -l || true
  sudo journalctl -u "$SERVICE_NAME" -n 100 --no-pager -l || true
  exit 1
fi

echo "=== 배포 완료 ==="

# 신규 API 스모크 테스트 (실패해도 배포 자체는 성공으로 유지)
echo "=== 스모크 테스트 ==="
smoke_test() {
  local method="$1" path="$2"
  local code
  code=$(curl --silent --show-error --output /tmp/smoke_body \
      --connect-timeout 2 --max-time 5 \
      -o /tmp/smoke_body -w '%{http_code}' \
      -X "$method" "http://localhost:8080${path}")
  echo "[$method $path] HTTP $code"
  head -c 300 /tmp/smoke_body; echo
}

smoke_test GET "/api/products"
smoke_test GET "/api/products/popular"

exit 0
