package com.plee.library.config;

import com.plee.library.customUserDetails.CustomUserDetailsService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class TestUserDetailsConfig {

    @Bean
    public CustomUserDetailsService testUserDetailService() {
        return new CustomUserDetailsService();
    }
}
