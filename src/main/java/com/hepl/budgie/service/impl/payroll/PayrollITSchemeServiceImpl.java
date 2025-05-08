package com.hepl.budgie.service.impl.payroll;

import java.util.*;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollITSchemeDTO;
import com.hepl.budgie.dto.payroll.PayrollTypeDTO;
import com.hepl.budgie.entity.payroll.ITScheme;
import com.hepl.budgie.entity.payroll.PayrollITScheme;
import com.hepl.budgie.repository.payroll.PayrollITSchemeRepository;
import com.hepl.budgie.service.payroll.PayrollITSchemeService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollITSchemeServiceImpl implements PayrollITSchemeService {

    private final JWTHelper jwtHelper;
    private final PayrollITSchemeRepository payrollITSchemeRepository;
    private final MongoTemplate mongoTemplate;

    @Override
    public PayrollITScheme savePayrollSection(String section, String description) {

        String orgId = jwtHelper.getOrganizationCode();
        PayrollITScheme payrollITScheme = payrollITSchemeRepository.findByType(section, orgId, mongoTemplate, "IN");
        if (payrollITScheme != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_TYPE_EXISTS);
        }
        return payrollITSchemeRepository.savePayrollSection(section, description, orgId, mongoTemplate, "IN");

    }

    @Override
    public PayrollITScheme savePayrollITScheme(PayrollITSchemeDTO scheme) {

        String orgId = jwtHelper.getOrganizationCode();
        PayrollITScheme payrollITScheme = payrollITSchemeRepository.findByType(scheme.getType(), orgId, mongoTemplate,
                "IN");
        if (payrollITScheme == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.PAYROLL_TYPE_NOT_FOUND);
        }
        String slug = generateSlug(scheme.getTitle());

        if (payrollITScheme.getSchemes() != null) {
            boolean exists = payrollITScheme.getSchemes().stream()
                    .anyMatch(s -> s.getTitle().equalsIgnoreCase(scheme.getTitle()));
            if (exists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PAYROLL_SCHEME_EXISTS);
            }
        }
        String schemeId = payrollITSchemeRepository.getNextITSchemeIdFromDB(orgId, mongoTemplate, "IN");
        return payrollITSchemeRepository.savePayrollScheme(scheme, slug, orgId, schemeId, mongoTemplate, "IN");
    }

    private String generateSlug(String input) {
        return input.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
    }

    @Override
    public List<PayrollTypeDTO> getPayrollType() {

        log.info("fetch Payroll IT Scheme");
        String orgId = jwtHelper.getOrganizationCode();
        return payrollITSchemeRepository.getPayrollType(mongoTemplate, orgId, "IN");
    }

    @Override
    public List<ITScheme> getPayrollSchemes(String type) {

        log.info("fetch Payroll IT Scheme by Type: {}", type);
        String orgId = jwtHelper.getOrganizationCode();
        return payrollITSchemeRepository.findByType(type, orgId, mongoTemplate, "IN").getSchemes();
    }

}
