package com.wedu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * WEDU 백엔드 애플리케이션 진입점.
 *
 * <p>단일 배포 단위(모듈러 모놀리스)로 여러 Bounded Context(user, recommendation, product,
 * proposal, planner, community)를 한 프로세스에서 구동한다. 컨텍스트 간 결합은 패키지 경계로 관리한다.
 */
@SpringBootApplication
public class WeduApplication {

    public static void main(String[] args) {
        SpringApplication.run(WeduApplication.class, args);
    }
}
