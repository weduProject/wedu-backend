#!/bin/bash

APP_DIR=$(dirname "$(readlink -f "$0")")
APP_NAME="wedu-backend"
JAR_FILE=$(ls -t "$APP_DIR"/*.jar 2>/dev/null | head -1)
LOG_FILE="$APP_DIR/app.log"

if [ -z "$JAR_FILE" ]; then
  echo "JAR 파일을 찾을 수 없습니다."
  exit 1
fi

echo "=== $APP_NAME 배포 시작 ==="
echo "JAR: $JAR_FILE"

# 기존 프로세스 종료
PID=$(pgrep -f "$APP_NAME.*\.jar")
if [ -n "$PID" ]; then
  echo "기존 프로세스 종료 (PID: $PID)"
  kill -15 "$PID"
  sleep 5
  # graceful shutdown 실패 시 강제 종료
  if kill -0 "$PID" 2>/dev/null; then
    kill -9 "$PID"
  fi
fi

# 애플리케이션 실행
# prod 는 application-prod.yml 에서 CORS_ALLOWED_ORIGINS 가 필수.
# 서버에 환경변수가 없으면 로컬 프론트(Vite) 기본 Origin 을 사용한다.
export CORS_ALLOWED_ORIGINS="${CORS_ALLOWED_ORIGINS:-http://localhost:5173}"

nohup java \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  -jar "$JAR_FILE" \
  > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "새 프로세스 시작 (PID: $NEW_PID)"

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
  echo "헬스체크 실패 — 로그를 확인하세요: $LOG_FILE"
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
