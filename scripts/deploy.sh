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
nohup java -jar \
  -Dspring.profiles.active=prod \
  -Dfile.encoding=UTF-8 \
  "$JAR_FILE" \
  > "$LOG_FILE" 2>&1 &

NEW_PID=$!
echo "새 프로세스 시작 (PID: $NEW_PID)"

# 헬스체크 (최대 30초)
for i in $(seq 1 30); do
  sleep 1
  if curl -s http://localhost:8080/actuator/health > /dev/null 2>&1; then
    echo "=== 배포 완료 ==="
    exit 0
  fi
done

echo "헬스체크 실패 — 로그를 확인하세요: $LOG_FILE"
exit 1
