# 코드 스타일 가이드 (Java / Spring)

[ARCHITECTURE.md](../../ARCHITECTURE.md)가 "무엇을 어느 계층에" 를 정한다면, 이 문서는 "어떻게 쓰는가"의 구체 규약이다. 계층 원칙과 겹치는 내용은 반복하지 않는다.

## 네이밍

| 대상 | 규칙 | 예 |
|---|---|---|
| 컨트롤러 | `XxxController` | `UserController` |
| 애플리케이션 서비스 | `XxxService` | `UserService` |
| Repository 포트(domain) | `XxxRepository` | `UserRepository` |
| JPA 어댑터(infra) | `XxxRepositoryImpl` | `UserRepositoryImpl` |
| Spring Data 기술 인터페이스 | `XxxJpaRepository` | `UserJpaRepository` |
| 요청 DTO | `XxxRequest` | `UpdateProfileRequest` |
| 응답 DTO | `XxxResponse` | `UserProfileResponse` |
| 애플리케이션 경계 결과 | `XxxResult` | `UserProfileResult` |

- 애그리게이트/VO 는 도메인 언어(Ubiquitous Language) 그대로. 축약 지양(`usr` ✗, `user` ✓).
- 불리언은 `isXxx`/`hasXxx`. enum 상수는 대문자 스네이크(`KAKAO`).

## Lombok

- 엔티티/애그리게이트: `@Getter` + `@NoArgsConstructor(access = AccessLevel.PROTECTED)` 까지만.
- `@Setter`·`@Data`·`@AllArgsConstructor` 를 애그리게이트에 쓰지 않는다(불변식 보호). 생성은 정적 팩토리로.
- VO 는 `@Getter @EqualsAndHashCode`(값 동등성) + 검증 생성자.
- 서비스/컴포넌트 의존성 주입은 `@RequiredArgsConstructor` + `private final`.

## DTO

- 요청/응답/경계 결과는 모두 `record`.
- 변환 책임: `Response.from(Result)`, `Result.from(Aggregate)` 처럼 **받는 쪽 정적 팩토리**에 둔다.
- 컨트롤러는 DTO ↔ 서비스 호출만. 응답은 `ApiResponse.ok(...)` 로 감싼다.

## 엔티티 / JPA

- `@Entity` 는 애그리게이트 루트에. 공통 시각 필드는 `BaseTimeEntity` 상속.
- 식별자: `@Id @GeneratedValue(strategy = IDENTITY)` (MySQL auto-increment).
- 연관: **다른 애그리게이트를 `@ManyToOne` 으로 물지 않는다.** id 필드(`Long userId`)로 참조.
  같은 애그리게이트 내부 구성요소만 `@OneToMany`/`@Embedded` 로 묶는다.
- 쿼리: 네이티브 SQL 금지. `@Query`(JPQL) 또는 Spring Data 메서드 파생. 멀티컬럼 결과는
  `Object[]` 로 받지 말고 JPQL constructor expression 으로 record/DTO 에 직접 매핑.
- 시각은 UTC. 응답 직렬화는 ISO-8601.

## 예외 / 검증

- 형식 검증(필수/길이 등 표현 계층): 요청 DTO 에 `@NotBlank` 등 + 컨트롤러 `@Valid`.
- 도메인 규칙 검증: 애그리게이트/VO 생성·변경 시점에서 `BusinessException(ErrorCode.XXX)`.
- 새 에러는 `ErrorCode` 에 도메인 접두어로 추가하고 `code` 는 한 번 정하면 바꾸지 않는다.

## 테스트

- 클래스명 `XxxTest`, 메서드는 한글 `@DisplayName` 으로 시나리오를 서술.
- 값 검증(assert)을 먼저 명확히. AssertJ(`assertThat`) 사용.
- 도메인 규칙: Spring 없는 순수 단위 테스트. 유스케이스: `@ExtendWith(MockitoExtension.class)` + 포트 mock.
- 통합/컨텍스트: `@SpringBootTest`(H2 테스트 프로파일). 외부 MySQL 의존 없이 돌게 한다.
- 기능 구현 전에 테스트를 먼저 쓴다(TDD: red → green → refactor).

## 주석

- 자명한 코드에 주석 달지 않는다. 비자명한 **왜(why)** 와 함정(gotcha)만 남긴다.
- 공개 타입/도메인 메서드는 의도가 드러나면 짧은 KDoc/Javadoc. 레퍼런스는 `com.wedu.user` 의 주석 밀도를 기준으로.
