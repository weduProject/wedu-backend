# CLAUDE.md — WEDU Backend

Claude Code 및 AI 에이전트가 이 저장소에서 작업할 때의 지침. 팀 공용 규칙은
[AGENTS.md](../AGENTS.md), 구조 원칙은 [ARCHITECTURE.md](../ARCHITECTURE.md)에 있다. **코드 작성 전 두 문서를 먼저 읽는다.**

## 이 프로젝트

심리테스트 기반 프로포즈 추천 플랫폼(WEDU)의 백엔드. Java 21 / Spring Boot 3.3.5 / Gradle / MySQL / JPA.
도메인별 패키지 + 계층형 아키텍처(실용적 DDD). 6개 Bounded Context, 각 컨텍스트는
`presentation / application / domain / infrastructure` 4계층.

## 반드시 지킬 것

- **계층 책임 준수**: 비즈니스 규칙은 domain 애그리게이트에, 트랜잭션은 application 에, 변환은 presentation 에.
  application 서비스가 두꺼워지면 규칙을 domain 으로 내린다.
- **애그리게이트에 setter 금지**. 상태 변경은 의도를 드러내는 도메인 메서드로.
- **DTO 는 record**, 도메인 노출 금지. 컨텍스트 간 참조는 id 로.
- **예외는 `BusinessException` + `ErrorCode`**, HTTP 매핑은 `GlobalExceptionHandler` 한 곳.
- **레퍼런스는 `com.wedu.user`**. 새 코드는 이 구조/스타일을 그대로 따른다.
- **TDD 지향**: 기능 전 테스트 먼저. 도메인 규칙은 Spring 없는 단위 테스트로.

## 작업 방식

- 수정(Edit/Write) 전에 **무엇을/왜/어떻게** 간단히 설명하고 진행한다. 읽기·빌드·조회는 자유.
- 코드/파일을 언급할 때 실제 경로(`파일:라인`)를 포함한다. 추측하지 않고, 모르면 "찾지 못함"이라고 말한다.
- **커밋/푸시는 사람이 확인 후.** `main` 직접 push 금지 — `feature/*` 브랜치 + PR.
- 커밋 전 `./gradlew clean build` 통과를 확인한다.
- 비밀값(DB 비밀번호, OAuth secret, JWT secret)은 커밋하지 않는다.

## 가이드 / 스킬

- 구체 코드 스타일(네이밍·Lombok·JPA·테스트): [.claude/guides/code-style.md](./guides/code-style.md)
- PR 생성 절차(컨벤션·draft PR): [.claude/skills/create-pr/SKILL.md](./skills/create-pr/SKILL.md)

## 자주 쓰는 명령

```bash
./gradlew clean build   # 컴파일 + 테스트
./gradlew test          # 테스트
./gradlew bootRun       # 실행
```
