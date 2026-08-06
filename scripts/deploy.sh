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
  if curl --fail --silent --show-error \
      --connect-timeout 2 --max-time 5 \
      http://localhost:8080/api/products > /dev/null; then
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
