package com.hepl.budgie.config.auditing;

import java.util.Optional;

import org.springframework.data.domain.AuditorAware;

import com.hepl.budgie.config.security.JWTHelper;

public class AuditAwareImpl implements AuditorAware<String> {

    private final JWTHelper jwtHelper;

    public AuditAwareImpl(JWTHelper jwtHelper) {
        this.jwtHelper = jwtHelper;
    }

    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.ofNullable(jwtHelper.getUserRefDetail().getEmpId());
    }

}
