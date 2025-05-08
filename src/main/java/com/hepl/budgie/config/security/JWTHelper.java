package com.hepl.budgie.config.security;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.UserRef;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class JWTHelper {

    private static final String USER_DETAILS = "userdetails";
    private final JwtDecoder jwtDecoder;
    private final JwtEncoder jwtEncoder;

    public JWTHelper(JwtDecoder jwtDecoder, JwtEncoder jwtEncoder) {
        this.jwtDecoder = jwtDecoder;
        this.jwtEncoder = jwtEncoder;
    }

    public String createJwtForClaims(String subject, Map<String, Object> claimMap, List<String> authorities) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder().issuer("Budgie").issuedAt(now).subject(subject)
                .expiresAt(now.plus(1, ChronoUnit.DAYS)).claim(USER_DETAILS, claimMap)
                .claim("authorities", authorities)
                .claim("client", "budgie")
                .build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    public String getOrganizationCode() {
        return getUserRefDetail().getOrganizationCode();
    }

    public String getOrganizationGroupCode() {
        return getUserRefDetail().getOrganizationGroupCode();
    }

    public Map<String, Object> getDetails(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Jwt decode = jwtDecoder.decode(jwt.getTokenValue());
        return decode.getClaimAsMap(USER_DETAILS);
    }

    public UserRef getUserRefDetail() {
        Map<String, Object> details = getDetails(SecurityContextHolder.getContext().getAuthentication());
        UserRef user = new UserRef();
        user.setEmpId(details.get("empId").toString());
        user.setOrganizationCode(details.get("organizationCode").toString());
        user.setOrganizationGroupCode(details.get("groupId").toString());
        user.setActiveRole(details.get("activeRole").toString());
        return user;
    }

}
