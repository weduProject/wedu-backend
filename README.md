# 💍 WEDU Backend

WEDU는 심리테스트 기반 맞춤 추천으로 사용자에게 프로포즈 상품과 준비 과정을 제안하는 플랫폼이다.
사용자는 성향 테스트를 하고, 추천받은 상품으로 나만의 프로포즈 견적을 만들고, D-day·체크리스트·예산으로 준비 일정을 관리한다.

이 저장소는 그 백엔드 서버다. 아키텍처 원칙은 [ARCHITECTURE.md](./ARCHITECTURE.md)에, 기여 규칙은 [AGENTS.md](./AGENTS.md)에 있다.

---

## 기술 스택

| 구분 | 기술 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 3.3.5 (Spring MVC) |
| 빌드 | Gradle (wrapper 포함) |
| 저장소 | MySQL + Spring Data JPA (테스트는 H2) |
| 인증 | 소셜 로그인(OAuth2: Kakao/Naver/Google) + JWT |
| API 문서 | springdoc-openapi (Swagger UI) |
| 테스트 | JUnit 5, Mockito, AssertJ |

---

## 아키텍처 요약

**도메인별 패키지 + 계층형 아키텍처(DDD)**. 기능은 6개 Bounded Context로 묶여 있고, 각 컨텍스트는
`presentation / application / domain / infrastructure` 4계층으로 나뉜다. 자세한 근거·규칙은 [ARCHITECTURE.md](./ARCHITECTURE.md).

| 패키지 | 도메인 | 기능(ID) | 담당 |
|---|---|---|---|
| `user` | 사용자/인증 | 회원관리(001)·온보딩(002)·마이페이지(018) | 미미 |
| `recommendation` | 테스트/추천 | 심리테스트(003)·맞춤추천(004) | 다은 |
| `product` | 상품 | 편집샵(006)·상품상세(007)·인기/크롤링(008) | 완규 |
| `proposal` | 견적/장바구니 | 나만의 프로포즈(009)·견적/장바구니(010)·찜(011) | 완규 |
| `planner` | 일정관리 | D-day(012)·캘린더(013)·체크리스트(014)·예산(015) | 경환 |
| `community` | 커뮤니티 | 후기/평점(017)·커뮤니티(016·후순위) | 다은 |

`user` 컨텍스트가 전 계층을 채운 **레퍼런스 구현**이다. 새 기능은 이 구조를 본떠 자기 컨텍스트 안에 만든다.

---

## 실행

### 사전 준비
- JDK 21
- MySQL (로컬 실행 또는 접속 정보) — 없으면 아래 환경변수로 접속 정보를 주입한다.

### 환경변수
민감값(DB 비밀번호, OAuth client-secret, JWT secret)은 커밋하지 않는다. 환경변수 또는
`application-secret.yml`(gitignore 처리됨)로 주입한다.

```bash
export DB_URL="jdbc:mysql://localhost:3306/wedu?serverTimezone=UTC&characterEncoding=UTF-8"
export DB_USERNAME="root"
export DB_PASSWORD="****"
export JWT_SECRET="256bit 이상 랜덤 문자열"
# 로컬 첫 실행이라 스키마 자동 생성이 필요하면:
export JPA_DDL_AUTO="update"
```

### 명령어

```bash
# 실행
./gradlew bootRun

# 전체 빌드 (컴파일 + 테스트)
./gradlew clean build

# 테스트만
./gradlew test

# 실행 가능한 JAR 생성 → build/libs/*.jar
./gradlew bootJar
```

### API 문서
서버 실행 후 Swagger UI: `http://localhost:8080/swagger-ui.html`

---

## 브랜치 & 커밋 규칙

- **작업 시작 전 이슈 먼저 생성.** GitHub 이슈로 목적·범위를 적고, PR 본문 `이슈: Closes #<번호>` 로 연결한다(머지 시 자동 종료).
- **`main` 직접 push 금지.** 모든 변경은 PR로 병합한다(브랜치 보호 적용).
- 작업 브랜치: `<type>/<요약>` — `type` 은 아래 커밋 컨벤션과 동일하게 `feat` · `fix` · `docs` · `refactor` · `test` · `chore` 중 하나.
  - 예: `feat/product-search`, `fix/user-login-npe`, `chore/coderabbit-config`
  - 도메인이 뚜렷하면 요약 앞에 도메인을 붙인다(`feat/product-search`).
- 커밋 컨벤션: `feat` · `fix` · `docs` · `refactor` · `test` · `chore`
  - 예: `feat: 상품 검색 필터 추가 (WEDU-006)`
  - 브랜치 · 커밋 타입 · 이슈/PR 라벨(✨ feat · 🐛 fix · 📝 docs · ♻️ refactor · ✅ test · 🔧 chore)은 같은 타입 체계를 쓴다.

PR을 열면 [PR 템플릿](./.github/PULL_REQUEST_TEMPLATE.md)이 자동으로 채워진다.

---

## 라이선스

학습 목적으로 개발된 프로젝트다.
