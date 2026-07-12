package com.wedu;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 스프링 컨텍스트가 정상적으로 로드되는지 검증하는 스모크 테스트.
 * 테스트 프로파일(H2)로 뜨므로 외부 MySQL 없이 실행된다.
 */
@SpringBootTest
class WeduApplicationTests {

    @Test
    void contextLoads() {
    }
}
