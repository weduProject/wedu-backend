# Database provisioning

운영 환경은 `spring.jpa.hibernate.ddl-auto=validate`를 사용하므로, 배포 전에 이 디렉터리의
SQL 파일을 버전 번호 순서대로 MySQL에 적용한다. 적용 여부와 실행 이력은 배포 담당자가 관리한다.

```bash
mysql -h <host> -u <user> -p <database> < scripts/migrations/V001__create_ddays.sql
```
