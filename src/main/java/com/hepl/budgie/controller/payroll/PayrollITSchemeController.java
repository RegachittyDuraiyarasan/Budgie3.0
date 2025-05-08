package com.hepl.budgie.controller.payroll;

import java.util.*;

import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollITSchemeDTO;
import com.hepl.budgie.dto.payroll.PayrollTypeDTO;
import com.hepl.budgie.entity.payroll.ITScheme;
import com.hepl.budgie.entity.payroll.PayrollITScheme;
import com.hepl.budgie.service.payroll.PayrollITSchemeService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Payroll IT Scheme", description = "Create and Manage the Payroll IT Scheme")
@SecurityRequirement(name = "Bearer Authentication")
@RequestMapping("/payroll/it-scheme")
@RestController
@Slf4j
@RequiredArgsConstructor
public class PayrollITSchemeController {

    private final PayrollITSchemeService payrollITSchemeService;
    private final Translator translator;

    @PostMapping("/type")
    public GenericResponse<PayrollITScheme> savePayrollSection(@RequestParam String type, @RequestParam String description) {

        log.info("Saving Payroll IT Scheme for section: {}", type);
        PayrollITScheme sections = payrollITSchemeService.savePayrollSection(type, description);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_IT_SECTION_SAVED), sections);
    }

    @PostMapping()
    public GenericResponse<PayrollITScheme> savePayrollITScheme(@RequestBody PayrollITSchemeDTO scheme) {

        log.info("Saving Payroll IT Scheme for section: {}", scheme);
        PayrollITScheme schemes = payrollITSchemeService.savePayrollITScheme(scheme);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_IT_SCHEME_SAVED), schemes);
    }

    @GetMapping()
    public GenericResponse<List<PayrollTypeDTO>> getPayrollType() {

        log.info("fetch Payroll IT Scheme for section: {}");
        List<PayrollTypeDTO> schemes = payrollITSchemeService.getPayrollType();
        return GenericResponse.success(schemes);
    }

    @GetMapping("/type")
    public GenericResponse<List<ITScheme>> getPayrollSchemes(@RequestParam String type) {

        log.info("fetch Payroll IT Scheme by Type: {}", type);
        List<ITScheme> schemes = payrollITSchemeService.getPayrollSchemes(type);
        return GenericResponse.success(schemes);
    }
}
