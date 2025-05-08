package com.hepl.budgie.config.auditing;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

import com.hepl.budgie.config.security.JWTHelper;

@Configuration
@EnableMongoAuditing
public class AuditingConfig {

    private final JWTHelper jwtHelper;

    public AuditingConfig(JWTHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Bean
    AuditorAware<String> myAuditorProvider() {
        return new AuditAwareImpl(jwtHelper);
    }

}
