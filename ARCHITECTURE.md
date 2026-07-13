# WEDU Backend 아키텍처 가이드

이 문서는 WEDU 백엔드를 어떤 구조로 짤지 정해둔 규칙입니다. 처음 하는 사람도
"이 코드는 어느 클래스에 써야 하지?"를 바로 답할 수 있게 쓰는 게 목표입니다.

한 줄 요약: **Spring MVC 그대로 갑니다.** Controller → Service → Repository → Entity
4단으로 흐르고, 도메인별로 패키지를 나눕니다. DDD 이론은 몰라도 됩니다. 딱 하나,
"Entity가 자기 데이터 규칙은 스스로 지킨다"만 챙기면 됩니다.

## 기술 스택

- Java 21, Spring Boot 3.3.5, Gradle
- MySQL + Spring Data JPA (테스트는 H2)
- OAuth2 + JWT 인증 (세션 없음, stateless)

## 패키지 구조

도메인(기능) 단위로 먼저 나누고, 그 안에서 레이어로 나눕니다.
"레이어 먼저(controller/, service/ ...)"가 아니라 "도메인 먼저"입니다.

```
com.wedu
├── global                      // 도메인이 아닌 공통 기술 코드
│   ├── config                  //   스프링 설정
│   ├── security                //   인증/인가 (JWT, OAuth2)
│   ├── error                   //   BusinessException, ErrorCode, GlobalExceptionHandler
│   ├── response                //   ApiResponse (공통 응답 껍데기)
│   └── common                  //   BaseEntity 등 진짜 공용
│
└── <도메인>                     // user, recommendation, product, proposal, planner, community
    ├── controller              //   REST 엔드포인트
    ├── dto                     //   요청/응답 DTO (record)
    ├── service                 //   비즈니스 로직
    ├── domain                  //   Entity (JPA)
    └── repository              //   DB 접근 (Spring Data JPA)
```

새 기능을 짤 때는 위 5개 폴더를 그 도메인 안에 만들고 채워 넣으면 됩니다.

## 흐름

요청 하나가 이렇게 흐릅니다. 화살표 방향으로만 부릅니다. 거꾸로(예: Repository가
Service를 부르는 것) 부르면 안 됩니다.

```
HTTP 요청
  → Controller   (요청 받기, 검증, DTO로 변환)
  → Service      (비즈니스 로직, 트랜잭션)
  → Repository   (DB 읽고 쓰기)
  → Entity/DB
  ← ...          (반대로 응답 DTO 만들어서 돌려줌)
```

## 레이어별 책임 (여기가 핵심)

### 1. Controller — "HTTP 통역사"

**하는 일**
- URL/HTTP 메서드 매핑 (`@GetMapping` 등)
- 요청 값 검증 (`@Valid`)
- 로그인 사용자 꺼내기 (`@AuthenticationPrincipal`)
- Service 호출하고, 결과를 `ApiResponse`로 감싸서 반환

**하면 안 되는 일**
- `if`로 비즈니스 규칙 판단 (예: "포인트가 모자라면...") → Service로
- Repository 직접 호출 → Service를 거쳐야 함
- `@Transactional` 붙이기 → Service에만
- Entity를 그대로 응답으로 내보내기 → 반드시 응답 DTO로

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @PostMapping
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request,
                                               @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(productService.create(request, userId));
    }
}
```

### 2. Service — "실제 일하는 곳"

**하는 일**
- 유스케이스 하나 = public 메서드 하나 (예: `create`, `cancel`, `getDetail`)
- 여러 Repository를 조합해서 로직 수행
- 트랜잭션 경계 (`@Transactional`은 여기)
- 요청 DTO → Entity, Entity → 응답 DTO 변환
- 규칙 위반이면 `BusinessException` 던지기

**하면 안 되는 일**
- HttpServletRequest, ResponseEntity 같은 웹 타입 만지기 → 그건 Controller 것
- 메서드 하나가 너무 많은 일 하기 → 유스케이스 단위로 쪼개기

```java
@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    @Transactional
    public ProductResponse create(ProductCreateRequest request, Long userId) {
        Product product = Product.create(request.name(), request.price(), userId);
        productRepository.save(product);
        return ProductResponse.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponse getDetail(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }
}
```

읽기만 하는 메서드는 `@Transactional(readOnly = true)`를 붙입니다.

### 3. Repository — "DB 창구"

**하는 일**
- `JpaRepository`를 상속한 인터페이스만
- 간단한 조회는 메서드 이름으로 (`findByUserId`)
- 복잡한 쿼리는 `@Query`(JPQL)로. **네이티브 SQL 금지** (DB 종속 함수는 H2 테스트에서 깨짐)

**하면 안 되는 일**
- 여기에 비즈니스 로직 넣기 → Service로
- 구현 클래스 직접 만들기 (Querydsl 등 필요할 때만 별도 논의)

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByUserId(Long userId);
}
```

### 4. Entity — "테이블 + 자기 규칙"

**하는 일**
- 테이블 매핑 (`@Entity`, `@Column`)
- **자기 데이터의 규칙은 자기가 지킨다.** setter를 열지 말고, 의미 있는 메서드로 바꿉니다.
  (여기까지가 우리가 챙기는 딱 한 스푼의 DDD입니다.)

```java
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)   // 아무나 new 못 하게
public class Product extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private int price;
    private Long userId;

    private Product(String name, int price, Long userId) {
        if (price < 0) throw new BusinessException(ErrorCode.INVALID_PRICE);  // 규칙을 생성 시점에
        this.name = name;
        this.price = price;
        this.userId = userId;
    }

    // setProduct(...) 대신 의도가 드러나는 메서드
    public static Product create(String name, int price, Long userId) {
        return new Product(name, price, userId);
    }
    public void changePrice(int newPrice) {
        if (newPrice < 0) throw new BusinessException(ErrorCode.INVALID_PRICE);
        this.price = newPrice;
    }
}
```

> 부담되면 처음엔 그냥 필드 + getter만 있는 Entity로 시작해도 됩니다. `setXxx` 남발만
> 피하세요. 규칙이 생기면 그때 위처럼 메서드로 감싸면 됩니다.

### 5. DTO — "바깥과 주고받는 껍데기"

- 요청/응답 전용. `record`로 만듭니다.
- **Entity를 그대로 컨트롤러 밖으로 내보내지 않습니다.** (JPA 지연로딩, 필드 노출 사고 방지)
- Entity → DTO 변환은 DTO 안에 `from(...)` 정적 메서드로 두면 깔끔합니다.

```java
public record ProductResponse(Long id, String name, int price) {
    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getName(), p.getPrice());
    }
}
```

## 공통 규칙

- **응답은 항상 `ApiResponse`로 감쌉니다.** (성공/실패 포맷 통일)
- **예외는 `BusinessException` + `ErrorCode`로 던집니다.** HTTP 상태 코드로 바꾸는 건
  `GlobalExceptionHandler`가 한 곳에서 처리합니다. Controller에서 try-catch 하지 마세요.
- **시간은 전부 UTC.** `BaseEntity`에 JPA Auditing으로 `createdAt`/`updatedAt`을 둡니다.
- **다른 도메인은 ID로만 참조합니다.** 예: 주문이 사용자를 알아야 하면 `User` 객체가
  아니라 `Long userId`를 들고, 필요하면 UserService를 통해 조회합니다.
  (도메인끼리 Entity를 직접 물면 나중에 얽혀서 못 풀어요.)

## "이건 어디에 두나요?" 빠른 표

| 하고 싶은 것 | 두는 곳 |
|---|---|
| URL 매핑, 요청 검증 | Controller |
| 로그인 사용자 꺼내기 | Controller (`@AuthenticationPrincipal`) |
| "포인트가 모자라면 실패" 같은 규칙 | Service, 또는 규칙이 Entity 자기 것이면 Entity |
| 트랜잭션 (`@Transactional`) | Service |
| DB 조회/저장 | Repository |
| 쿼리 직접 작성 | Repository (`@Query` JPQL) |
| 요청/응답 형태 | dto (record) |
| 여러 도메인 걸친 로직 | Service (필요한 Repository/다른 Service 조합) |
| 공통 에러/응답/설정/시큐리티 | global |

## 일부러 안 하는 것들 (지금은)

- CQRS, 이벤트 소싱 → 필요해지기 전엔 안 합니다.
- 헥사고날/포트-어댑터 같은 엄격한 구조 → 소규모·학습용이라 과합니다.
- Entity와 별도의 도메인 객체 분리 → JPA 어노테이션은 Entity에 바로 붙입니다.

규칙은 필요가 생기면 그때 추가합니다. 미리 복잡하게 만들지 않는 게 원칙입니다.

## 테스트

- **Service**: Repository를 Mockito로 mock 해서 로직만 검증.
- **Entity**: 스프링 없이 순수 단위 테스트 (규칙 메서드 검증).
- **통합**: `@SpringBootTest` + H2로 Controller~DB까지 한 번에.
- 가능하면 테스트 먼저 짜고(TDD) 구현하는 걸 권장합니다.
