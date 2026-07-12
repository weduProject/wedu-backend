# WEDU Backend 아키텍처

이 문서는 WEDU 백엔드의 구조 원칙을 정의한다. 코드를 짜기 전에 이 문서의 계층 책임과 의존성 규칙을 먼저 읽는다.

## 한눈에 보기

- **아키텍처**: 도메인별 패키지(Bounded Context) + 계층형 아키텍처(Layered Architecture). 단일 배포 단위의 모듈러 모놀리스다.
- **언어/런타임**: Java 21, Spring Boot 3.3.5, Gradle
- **저장소**: MySQL + Spring Data JPA (테스트는 H2)
- **인증**: 소셜 로그인(OAuth2) + JWT, 무상태(stateless)

우리는 "완전한 헥사고날"을 목표로 하지 않는다. 소규모 팀·학습 프로젝트에 맞게 **실용적 DDD**를 따른다. 핵심 원칙(계층 책임 분리, 의존성 방향, 리치 도메인 모델)은 지키되, 과한 추상화는 피한다.

## 근거로 삼은 원전

이 구조는 다음 문헌의 패턴을 근거로 한다. 판단이 애매하면 아래 순서로 참고한다.

- **Eric Evans, _Domain-Driven Design_ (2003)** — 계층형 아키텍처(Presentation/Application/Domain/Infrastructure), Entity·Value Object·Aggregate·Repository·Domain Service, Bounded Context, Ubiquitous Language.
- **Vaughn Vernon, _Implementing Domain-Driven Design_ (2013)** — 애그리게이트 설계 규칙: "작게 유지하고, 애그리게이트 간에는 객체 참조가 아니라 식별자(id)로 참조하며, 하나의 트랜잭션은 하나의 애그리게이트만 수정한다."
- **Robert C. Martin, _Clean Architecture_ (2017)** & **Alistair Cockburn, Hexagonal Architecture (Ports & Adapters)** — 의존성 규칙: 의존성은 바깥(인프라)에서 안(도메인)으로만 향한다. Repository 인터페이스(포트)는 도메인에, 구현(어댑터)은 인프라에 둔다.
- **Martin Fowler, _Anemic Domain Model_** — 비즈니스 규칙 없이 게터/세터만 있는 빈약한 엔티티는 안티패턴. 규칙은 애그리게이트 안에 둔다.

## 패키지 구조

```
com.wedu
├── WeduApplication.java          # 진입점
│
├── global                         # 공통(cross-cutting). 특정 도메인에 속하지 않는 기술 관심사
│   ├── common                     #   BaseTimeEntity 등 공유 상위 타입
│   ├── config                     #   Security / Swagger / JPA Auditing 설정
│   ├── error                      #   ErrorCode, BusinessException, GlobalExceptionHandler
│   ├── response                   #   ApiResponse 공통 응답 봉투
│   └── security                   #   JWT 발급/검증, 인증 필터
│
└── <bounded-context>              # 도메인별 패키지 (아래 6개)
    ├── presentation               #   REST 컨트롤러, 요청/응답 DTO
    ├── application                #   애플리케이션 서비스(유스케이스), 트랜잭션 경계, 경계 DTO
    ├── domain                     #   애그리게이트/엔티티, 값 객체, 도메인 서비스, Repository 포트, enum
    └── infrastructure             #   Repository 구현(JPA 어댑터), 외부 연동(크롤러 등)
```

### Bounded Context 목록

기획 기능표의 6개 도메인 그룹을 그대로 컨텍스트로 매핑했다. 각 컨텍스트의 상세 책임은 해당 패키지의 `package-info.java`에 적혀 있다.

| 패키지 | 도메인 | 기능(ID) | 담당 |
|---|---|---|---|
| `user` | 사용자/인증 | 회원관리(001)·온보딩(002)·마이페이지(018) | 미미 |
| `recommendation` | 테스트/추천 | 심리테스트(003)·맞춤추천(004) | 다은 |
| `product` | 상품 | 편집샵(006)·상품상세(007)·인기/크롤링(008) | 완규 |
| `proposal` | 견적/장바구니 | 나만의 프로포즈(009)·견적/장바구니(010)·찜(011) | 완규 |
| `planner` | 일정관리 | D-day(012)·캘린더(013)·체크리스트(014)·예산(015) | 경환 |
| `community` | 커뮤니티 | 후기/평점(017)·커뮤니티(016·후순위) | 다은 |

`user` 컨텍스트가 전 계층을 채운 **레퍼런스 구현**이다. 새 컨텍스트를 채울 때 이 구조를 그대로 본뜬다.

## 계층 책임

의존성은 **presentation → application → domain ← infrastructure** 방향이다. 도메인은 어떤 계층에도 의존하지 않는다.

### presentation (표현)
- REST 엔드포인트, 요청/응답 DTO, 입력 형식 검증(`@Valid`), 인증 principal 해석.
- 비즈니스 로직 금지. 애플리케이션 서비스에 위임하고 DTO 변환만 한다.
- 요청/응답 DTO는 `record` 로. 도메인 애그리게이트를 그대로 노출하지 않는다.

### application (애플리케이션)
- 유스케이스 오케스트레이션. `@Transactional` 로 트랜잭션 경계를 긋는다.
- 애그리게이트를 조회 → 도메인 메서드 호출 → 결과를 경계 타입(record)으로 반환.
- **얇게 유지한다.** if 문으로 도메인 규칙을 흉내 내기 시작하면, 그 규칙은 애그리게이트로 내려야 한다는 신호다.

### domain (도메인)
- 비즈니스 규칙의 집. 애그리게이트/엔티티, 값 객체, 도메인 서비스, Repository **포트(인터페이스)**, enum.
- 상태 변경은 도메인 메서드로만. `setter` 를 두지 않는다.
- 다른 애그리게이트는 **id 로만 참조**한다(객체 참조 금지).
- Spring/JPA 기술에 대한 의존을 최소화한다(§실용적 타협 참고).

### infrastructure (인프라)
- 도메인 포트의 구현(어댑터). Spring Data JPA repository 를 감싼다.
- 외부 연동(크롤링, 외부 API)도 여기. 기술 세부사항을 이 계층 안에 가둔다.

## 핵심 규칙

1. **애그리게이트가 규칙을 갖는다.** 생성은 정적 팩토리(`User.register(...)`), 변경은 의도를 드러내는 메서드(`completeOnboarding()`)로. 불변식은 생성/변경 시점에 강제한다.
2. **값 객체를 활용한다.** 식별자 없이 값으로 의미가 완결되는 개념(닉네임, 금액 등)은 VO 로. 생성자에서 유효성을 보장해 "유효하지 않은 인스턴스가 존재할 수 없게" 한다. (예: `user.domain.Nickname`)
3. **Repository 는 애그리게이트 루트당 하나.** 인터페이스는 domain, 구현은 infrastructure.
4. **컨텍스트 간 결합은 id 로.** 다른 컨텍스트의 애그리게이트를 직접 참조하지 않고 `userId`, `productId` 로 참조한다. 표시에 필요한 데이터는 조회 시점에 가져온다.
5. **예외는 `BusinessException` + `ErrorCode`.** 도메인/애플리케이션은 HTTP 를 모른다. HTTP 변환은 `GlobalExceptionHandler` 한 곳에서.
6. **응답은 `ApiResponse` 봉투로 통일.** 성공 `ApiResponse.ok(data)`, 실패는 핸들러가 `ApiResponse.fail(code, message)`.
7. **시간은 UTC.** `BaseTimeEntity` 의 생성/수정 시각은 JPA Auditing 으로 채우고 UTC 로 다룬다.

## 실용적 타협 (읽고 넘어가기)

- **JPA 애너테이션을 애그리게이트에 붙인다.** 순수 도메인 모델과 별도 JPA 엔티티를 두어 매핑하는 방식은 이 규모에 과하다. 대신 `@Entity` 를 애그리게이트에 두되, 도메인은 Spring Data repository 인터페이스에 의존하지 않는다(포트로 역전). 이 정도가 실용적 DDD 의 균형점이다.
- **CQRS/이벤트 소싱은 도입하지 않는다.** 필요해지기 전까지 단순 CRUD + 리치 도메인으로 충분하다.

## 테스트 전략

- **도메인 단위 테스트**: 애그리게이트/VO 규칙을 Spring 없이 빠르게 검증(`UserTest`, `NicknameTest`).
- **애플리케이션 테스트**: Mockito 로 Repository 포트를 대역 처리해 유스케이스 흐름 검증(`UserServiceTest`).
- **컨텍스트 로드/통합 테스트**: `@SpringBootTest` + H2(`WeduApplicationTests`). 외부 MySQL 없이 돈다.
- 기능을 짜기 전에 테스트를 먼저 쓴다(TDD 지향).
