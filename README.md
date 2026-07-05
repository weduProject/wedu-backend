# 💍 WEDU Backend

## 📖 프로젝트 소개

WEDU는 심리테스트 기반 AI 추천을 통해 사용자에게 맞춤형 프로포즈 상품과 서비스를 추천하는 웹 플랫폼입니다.

사용자는 호텔, 꽃, 레스토랑, 케이크, 반지, 촬영 등 다양한 프로포즈 상품을 탐색하고 예약할 수 있으며, AI 추천을 통해 자신에게 가장 적합한 프로포즈 스타일을 확인할 수 있습니다.

---

# 👨‍💻 Backend Tech Stack

| Category | Technology |
|----------|------------|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Build Tool | Gradle |
| Database | MySQL |
| ORM | Spring Data JPA |
| Authentication | OAuth2 (Kakao / Naver / Google) |
| API Documentation | Swagger (OpenAPI 3) |
| Version Control | Git / GitHub |

---

# 📂 Project Structure

```
backend
│
├── src
│   ├── main
│   │   ├── java
│   │   │   └── com.wedu
│   │   │       ├── auth
│   │   │       ├── user
│   │   │       ├── test
│   │   │       ├── recommendation
│   │   │       ├── product
│   │   │       ├── vendor
│   │   │       ├── reservation
│   │   │       ├── wishlist
│   │   │       ├── review
│   │   │       ├── common
│   │   │       └── config
│   │   │
│   │   └── resources
│   │       ├── application.yml
│   │       └── static
│   │
│   └── test
│
├── docs
│   ├── api.md
│   ├── erd.md
│   └── convention.md
│
├── build.gradle
├── settings.gradle
└── README.md
```

---

# 🚀 주요 기능

### 👤 회원 관리
- 카카오 로그인
- 네이버 로그인
- 구글 로그인
- JWT 인증
- 마이페이지

### 🧠 심리테스트
- 테스트 결과 저장
- 테스트 결과 조회

### 🤖 AI 추천
- 사용자 성향 분석
- 맞춤 프로포즈 추천

### 🛍 상품
- 상품 조회
- 상품 상세 조회
- 상품 검색

### 🏢 업체
- 업체 조회
- 업체 상세 조회

### ❤️ 찜
- 찜 등록
- 찜 삭제
- 찜 목록 조회

### 📅 예약
- 예약 생성
- 예약 조회
- 예약 취소

### ⭐ 리뷰
- 리뷰 작성
- 리뷰 조회

---

# 🗄 Database

ERD는 ERDCloud를 사용하여 설계하였습니다.

주요 테이블

- Users
- Vendors
- Categories
- Products
- ProductImages
- ProductOptions
- TestResults
- Recommendations
- Wishlists
- Reservations
- Reviews

---

# 📖 API

RESTful API를 기반으로 개발합니다.

주요 API

```
POST /api/auth/social-login

GET /api/users/me

POST /api/tests

GET /api/tests/me

GET /api/recommendations

GET /api/categories

GET /api/products

GET /api/products/{id}

GET /api/vendors

POST /api/wishlists

DELETE /api/wishlists/{id}

POST /api/reservations

GET /api/reservations/me

POST /api/reviews
```

---

# 🌿 Branch Strategy

```
main
```

- 최종 배포 브랜치

```
develop
```

- 개발 통합 브랜치

```
feature/*
```

- 기능 개발

예시

```
feature/login

feature/product

feature/recommend

feature/reservation
```

---

# ✅ Commit Convention

```
feat:
```

새로운 기능 추가

```
fix:
```

버그 수정

```
docs:
```

문서 수정

```
style:
```

코드 스타일 수정

```
refactor:
```

리팩토링

```
test:
```

테스트 코드

```
chore:
```

설정 변경

---

# 👥 Team

Backend

- Spring Boot
- MySQL
- REST API
- Swagger
- ERD 설계

---

# 📌 Repository

WEDU Backend Repository

```
Spring Boot 기반 백엔드 프로젝트
```

---

# 📄 License

This project is developed for educational purposes.
