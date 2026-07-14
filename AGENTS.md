# AGENTS.md

이 문서는 사람과 AI 에이전트가 WEDU 백엔드에서 작업할 때 따르는 규칙이다.
구조적 배경은 [ARCHITECTURE.md](./ARCHITECTURE.md)를 먼저 읽는다.

## 시작 전 필독

코드를 작성/수정하기 **전에** [ARCHITECTURE.md](./ARCHITECTURE.md)의 계층 책임과 핵심 규칙을 읽는다.
이 규칙들은 취향이 아니라 팀의 합의이므로 우회하지 않는다.

## 개발 명령어

```bash
./gradlew clean build      # 컴파일 + 테스트 (PR 전 반드시 통과)
./gradlew test             # 테스트만
./gradlew bootRun          # 로컬 실행
./gradlew bootJar          # 실행 JAR
```

## 새 기능을 추가하는 방법 (레퍼런스: `user` 도메인)

자기 도메인 패키지 안에서 5개 폴더를 채운다. `com.wedu.user` 를 그대로 본뜬다.

1. **domain** — 엔티티/enum. 규칙은 여기에. 상태 변경은 의도를 드러내는 메서드로, `setter` 금지.
2. **repository** — `XxxRepository extends JpaRepository<Xxx, Long>`. 기본 CRUD 는 상속으로,
   조회는 메서드 이름 파생 또는 `@Query`(JPQL). 별도 Impl/래퍼를 두지 않는다.
3. **service** — `@Service`. 유스케이스 하나 = public 메서드 하나. `@Transactional` 로 경계를 긋고,
   엔티티를 조회→도메인 메서드 호출→응답 DTO(record) 반환. 얇게 유지.
4. **dto** — 요청/응답 DTO(record). 엔티티를 API 로 그대로 노출하지 않고, 변환은 DTO 의 `from(...)` 에 모은다.
5. **controller** — `@RestController`. `@Valid` 로 형식 검증, service 호출, 응답은 `ApiResponse.ok(...)`.
   비즈니스 로직 금지, `@Transactional` 금지.

기능을 짜기 전에 **테스트를 먼저** 쓴다(TDD). 도메인 규칙은 Spring 없는 단위 테스트로.

## 코딩 컨벤션

- **네이밍**: 컨트롤러 `XxxController`, 서비스 `XxxService`, 리포지토리 `XxxRepository`(JpaRepository 상속).
- **DTO 는 `record`**. 요청/응답 모두. 엔티티를 API 로 그대로 노출하지 않는다.
- **Lombok**: 엔티티에 `@Getter` + `@NoArgsConstructor(access = PROTECTED)` 까지만.
  `@Setter`/`@Data` 는 엔티티에 쓰지 않는다(불변식 보호).
- **예외**: 도메인/서비스는 `BusinessException(ErrorCode.XXX)` 만 던진다.
  HTTP 매핑은 `GlobalExceptionHandler` 가 담당한다. 새 에러는 `ErrorCode` 에 도메인 접두어로 추가.
- **도메인 간 참조는 id 로**. 다른 도메인 엔티티를 직접 import 해 필드로 들고 있지 않는다.
- **시간은 UTC**, 응답 직렬화는 ISO-8601.
- **Raw SQL/네이티브 쿼리 지양** — JPQL 또는 Spring Data 메서드 파생을 쓴다.

## 비밀값

DB 비밀번호, OAuth client-secret, JWT secret 은 **커밋 금지**. 환경변수 또는
`application-secret.yml`(gitignore)로 주입한다. `application.yml` 에는 기본값/플레이스홀더만 둔다.

## Git / PR

- `main` 직접 push 금지. `<type>/<요약>` 브랜치에서 작업 → PR (`type` 은 커밋 타입과 동일: `feat|fix|docs|refactor|test|chore`, 예 `feat/product-search`).
- 커밋: `feat|fix|docs|refactor|test|chore: 요약 (WEDU-XXX)`.
- 브랜치 · 커밋 타입 · 이슈/PR 라벨은 같은 타입 체계(feat/fix/docs/refactor/test/chore)를 공유한다.
- PR 전 `./gradlew clean build` 가 통과해야 한다. 리뷰 1인 이상 승인 후 병합.
- PR 본문은 [.github/PULL_REQUEST_TEMPLATE.md](./.github/PULL_REQUEST_TEMPLATE.md) 를 채운다.

## AI 에이전트 유의

- 파일을 수정하기 전 관련 계층/컨벤션을 먼저 확인한다. 확신이 없으면 `user` 컨텍스트의 실제 코드를 근거로 삼는다.
- 추측하지 않는다. 모르면 "찾지 못함"이라고 말한다.
- 커밋/푸시는 사람이 확인한 뒤에 한다. 임의로 `main` 에 push 하지 않는다.
