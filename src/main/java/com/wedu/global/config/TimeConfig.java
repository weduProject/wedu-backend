package com.wedu.global.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 애플리케이션에서 현재 시각을 일관되게 사용하도록 UTC {@link Clock}을 제공한다. */
@Configuration
public class TimeConfig {

    /** 운영 코드와 테스트가 주입받아 사용하는 시스템 UTC 시계. */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
