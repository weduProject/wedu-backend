# Database migrations (Flyway)

스키마 변경은 **Flyway**가 앱 기동 시 적용한다.
마이그레이션 SQL은 [`src/main/resources/db/migration/`](../../src/main/resources/db/migration/)에 두고 JAR에 포함한다.

- JPA: `spring.jpa.hibernate.ddl-auto=validate` (스키마 생성 없음, 검증만)
- Flyway: `classpath:db/migration`의 `V00x__*.sql`을 버전 순으로 적용
- 이력 테이블: `flyway_schema_history`
- 기동 시 자동 baseline 없음 (`baseline-on-migrate=false`)

## 로컬 / 신규 빈 DB

별도 작업 없이 `bootRun` 하면 V001부터 미적용분까지 자동 적용된다.

```bash
./gradlew bootRun
```

## 기존 운영 DB (이미 수동으로 V001~V016 적용한 경우)

앱을 **처음 Flyway와 함께 배포하기 전에** 아래를 **한 번** 수행한다.
기동만으로 baseline 하지 않는다.

1. **검증**: 운영 스키마가 코드의 `V016`과 일치하는지 확인한다.  
   V016 미적용 DB에 baseline 16을 하면 중간 버전이 스킵될 수 있다.
2. **baseline (1회)**: `flyway_schema_history`가 없을 때만, 버전 `16`으로 baseline 한다.  
   V001~V016 SQL은 다시 실행되지 않고, 이후 기동부터 `V017+`만 적용된다.

Flyway CLI 예시 (연결 정보는 환경에 맞게 치환):

```bash
flyway \
  -url="jdbc:mysql://<host>:3306/<database>?serverTimezone=UTC&characterEncoding=UTF-8" \
  -user="<user>" \
  -password="<password>" \
  -baselineVersion=16 \
  baseline
```

Docker로 CLI를 쓸 때:

```bash
docker run --rm flyway/flyway:10 \
  -url="jdbc:mysql://<host>:3306/<database>?serverTimezone=UTC&characterEncoding=UTF-8" \
  -user="<user>" \
  -password="<password>" \
  -baselineVersion=16 \
  baseline
```

확인:

```bash
mysql -h <host> -u <user> -p <database> -e "SELECT installed_rank, version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

baseline 행의 `version`이 `16`이면 준비한 것이다. 그다음 앱을 배포·기동한다.

## 새 마이그레이션 추가

1. `src/main/resources/db/migration/V017__짧은_설명.sql` 추가 (번호 증가)
2. 앱 코드 변경과 **같은 PR**에 넣는다
3. 로컬에서 기동·테스트 후 배포 (배포 스크립트에 별도 `mysql <` 단계 없음)
4. `baseline-version` / 기동 설정은 바꾸지 않는다

## 이전 수동 적용 방식

예전에 `scripts/migrations/`에서 `mysql < V00x...`로 적용하던 절차는 Flyway로 대체되었다.
SQL 파일 위치만 classpath로 옮겼고, 파일명 규칙(`V00x__description.sql`)은 동일하다.
