# Database migrations (Flyway)

스키마 변경은 **Flyway**가 앱 기동 시 적용한다.
마이그레이션 SQL은 [`src/main/resources/db/migration/`](../../src/main/resources/db/migration/)에 두고 JAR에 포함한다.

- JPA: `spring.jpa.hibernate.ddl-auto=validate` (스키마 생성 없음, 검증만)
- Flyway: `classpath:db/migration`의 `V00x__*.sql`을 버전 순으로 적용
- 이력 테이블: `flyway_schema_history`

## 로컬 / 신규 빈 DB

별도 작업 없이 `bootRun` 하면 V001부터 미적용분까지 자동 적용된다.

```bash
./gradlew bootRun
```

## 기존 운영 DB (이미 수동으로 V001~V016 적용한 경우)

최초 Flyway 배포 전에 **스키마가 V016까지 반영된 상태**인지 확인한다.
DB에 테이블은 있는데 `flyway_schema_history`가 없으면, 기동 시
`baseline-on-migrate=true` + `baseline-version=16` 설정으로 **V016까지 적용된 것으로 baseline**한 뒤
이후 버전(V017+)만 실행한다. V001~V016 SQL은 다시 돌리지 않는다.

확인:

```bash
# 이력 테이블이 생겼는지
mysql -h <host> -u <user> -p <database> -e "SELECT * FROM flyway_schema_history ORDER BY installed_rank;"
```

V016 이전까지만 수동 적용된 DB에 baseline 16을 쓰면 V011~V016이 스킵될 수 있으므로,
**첫 Flyway 배포 전에 운영 스키마가 코드의 V016과 일치하는지** 반드시 맞춘다.

## 새 마이그레이션 추가

1. `src/main/resources/db/migration/V017__짧은_설명.sql` 추가 (번호 증가)
2. 앱 코드 변경과 **같은 PR**에 넣는다
3. 로컬에서 기동·테스트 후 배포 (배포 스크립트에 별도 `mysql <` 단계 없음)

## 이전 수동 적용 방식

예전에 `scripts/migrations/`에서 `mysql < V00x...`로 적용하던 절차는 Flyway로 대체되었다.
SQL 파일 위치만 classpath로 옮겼고, 파일명 규칙(`V00x__description.sql`)은 동일하다.
