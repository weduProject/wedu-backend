<!-- PR 제목 형식: feat|fix|docs|refactor|test|chore: 요약 (WEDU-XXX) -->

## 작업 내용
<!-- 무엇을 왜 바꿨는지 간단히. 작업 전 이슈를 먼저 만들고 그 번호를 적는다. -->
- 이슈: Closes #
- 기능: WEDU-
- 도메인:

## 변경 사항 (As-is / To-be)
<!-- 동작이 어떻게 달라지는지. 없으면 - N/A -->
- As-is:
- To-be:

## 체크리스트
- [ ] `./gradlew clean build` 통과 (컴파일 + 테스트)
- [ ] 계층 책임 준수 — 규칙은 domain, 트랜잭션은 application, 변환은 presentation ([ARCHITECTURE.md](../ARCHITECTURE.md))
- [ ] 새 로직에 테스트 추가 (도메인 규칙은 단위 테스트)
- [ ] 비밀값(secret/비밀번호/토큰) 커밋 없음
- [ ] 다른 컨텍스트를 id 로만 참조 (직접 애그리게이트 참조 없음)

## 테스트 방법
<!-- 리뷰어가 확인할 수 있는 절차. 없으면 - N/A -->
-

## 참고 / 스크린샷
<!-- 관련 문서, API 예시, 스크린샷 등. 없으면 - N/A -->
- N/A
