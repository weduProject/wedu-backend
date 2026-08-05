# Database provisioning

운영 환경은 `spring.jpa.hibernate.ddl-auto=validate`를 사용하므로, 배포 전에 이 디렉터리의
SQL 파일을 버전 번호 순서대로 MySQL에 적용한다. 적용 여부와 실행 이력은 배포 담당자가 관리한다.

```bash
mysql -h <host> -u <user> -p <database> < scripts/migrations/V001__create_ddays.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V002__create_calendar_events.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V003__create_checklist_items.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V004__create_budget_items.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V005__create_products.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V006__create_popular_products.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V007__create_proposals.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V008__create_carts.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V009__create_wishlists.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V010__alter_users_for_email_auth.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V011__create_community_posts.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V012__create_friendships.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V013__create_share_links.sql
mysql -h <host> -u <user> -p <database> < scripts/migrations/V014__create_community_comments.sql
```
