package com.plee.library.config;

import com.plee.library.config.testUserDetails.TestUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestUserDetailsConfig {

    @Bean
    public TestUserDetailsService testUserDetailService() {
        return new TestUserDetailsService();
    }
}
