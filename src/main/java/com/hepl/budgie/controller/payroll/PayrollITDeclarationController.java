package com.hepl.budgie.controller.payroll;

import java.util.*;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.HraDTO;
import com.hepl.budgie.dto.payroll.LetOutDTO;
import com.hepl.budgie.dto.payroll.PayrollDeclarationDTO;
import com.hepl.budgie.dto.payroll.PayrollRequestDTO;
import com.hepl.budgie.dto.payroll.PreviousEmploymentTaxDTO;
import com.hepl.budgie.dto.payroll.SchemeUpdateDTO;
import com.hepl.budgie.entity.payroll.FamilyList;
import com.hepl.budgie.entity.payroll.PayrollITDeclaration;
import com.hepl.budgie.service.payroll.PayrollITDeclarationService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Payroll IT Declaration", description = "Create and Manage the Payroll IT Declaration")
@RestController
@Slf4j
@RequestMapping("/payroll-it-declaration")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class PayrollITDeclarationController {

    private final PayrollITDeclarationService payrollITDeclarationService;
    private final Translator translator;

    @GetMapping("/release-list")
    public GenericResponse<List<Map<String, String>>> getEmployeesByPayrollRoleType() {

        log.info("fetch Employees by role type");
        List<Map<String, String>> schemes = payrollITDeclarationService.getEmployeesByPayrollRoleType();
        return GenericResponse.success(schemes);
    }

    @PostMapping("/release")
    public GenericResponse<List<PayrollITDeclaration>> releasePayrollITDeclaration(
            @RequestBody PayrollRequestDTO release) {
        log.info("Release Payroll IT Declaration");
        List<PayrollITDeclaration> payrollITDeclaration = payrollITDeclarationService
                .releasePayrollITDeclaration(release);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_IT_DECLARATION_CREATED),
                payrollITDeclaration);
    }

    @GetMapping("/re-release-list")
    public GenericResponse<List<PayrollITDeclaration>> getPayrollReReleaseList() {
        log.info("Get Payroll IT Declaration");
        List<PayrollITDeclaration> payrollITDeclaration = payrollITDeclarationService.getPayrollReReleaseList();
        return GenericResponse.success(payrollITDeclaration);
    }

    @PostMapping("/re-release")
    public GenericResponse<List<PayrollITDeclaration>> reReleasePayrollITDeclaration(
            @RequestBody PayrollRequestDTO release) {
        log.info("Release Payroll IT Declaration");
        List<PayrollITDeclaration> payrollITDeclaration = payrollITDeclarationService
                .reReleasePayrollITDeclaration(release);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_IT_DECLARATION_CREATED),
                payrollITDeclaration);
    }

    @GetMapping("/index")
    public GenericResponse<PayrollDeclarationDTO> getEmployeePayrollDeclaration() {

        log.info("fetch Payroll declaration for Employee");
        PayrollDeclarationDTO payrollDeclarationDTO = payrollITDeclarationService.getEmployeePayrollDeclaration();
        return GenericResponse.success(payrollDeclarationDTO);
    }

    @PutMapping("/regime")
    public GenericResponse<PayrollITDeclaration> updateRegime(@RequestParam String regime) {
        log.info("Release Payroll IT Declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateRegime(regime);
        return GenericResponse.success(translator.toLocale(AppMessages.REGIME_UPDATED), payrollITDeclaration);
    }

    @PutMapping("/update-scheme")
    public GenericResponse<PayrollITDeclaration> updateSchemes(@RequestBody SchemeUpdateDTO schemes) {

        log.info("Update Schemes in Payroll IT declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateSchemes(schemes);
        return GenericResponse.success(translator.toLocale(AppMessages.SCHEMES_UPDATED), payrollITDeclaration);

    }

    @PutMapping("/update-hra")
    public GenericResponse<PayrollITDeclaration> updateHra(@RequestParam String planId,
            @RequestBody @Valid HraDTO hra) {

        log.info("Update HRA in Payroll IT declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateHra(planId, hra);
        return GenericResponse.success(translator.toLocale(AppMessages.HRA_UPDATED), payrollITDeclaration);
    }

    @PutMapping("/update-let-out")
    public GenericResponse<PayrollITDeclaration> updateLetOut(@RequestParam String planId,
            @RequestBody @Valid LetOutDTO itLetOut) {

        log.info("Update Let Out in Payroll IT declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateLetOut(planId, itLetOut);
        return GenericResponse.success(translator.toLocale(AppMessages.LET_OUT_UPDATED), payrollITDeclaration);
    }

    @PutMapping("/update-metro")
    public GenericResponse<PayrollITDeclaration> updateMetro(@RequestParam String planId, @RequestParam String metro) {

        log.info("Update Metro in Payroll IT declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateMetro(planId, metro);
        return GenericResponse.success(translator.toLocale(AppMessages.METRO_UPDATED), payrollITDeclaration);
    }

    @PutMapping("/update/previous-employee")
    public GenericResponse<PayrollITDeclaration> updatePreviousEmployee(@RequestParam String planId,
            @RequestBody PreviousEmploymentTaxDTO employeeTax) {

        log.info("Update Previous Employee in Payroll IT declaration");
        PayrollITDeclaration updatedPayrollITDeclaration = payrollITDeclarationService
                .updatePreviousEmployee(planId, employeeTax);
        return GenericResponse.success(translator.toLocale(AppMessages.PREVIOUS_EMPLOYEE_UPDATED),
                updatedPayrollITDeclaration);
    }

    @PutMapping("/update/draft/previous-employee")
    public GenericResponse<PayrollITDeclaration> draftPreviousEmployee(@RequestParam String planId,
            @RequestBody PreviousEmploymentTaxDTO employeeTax) {

        log.info("Update Previous Employee in Payroll IT declaration");
        PayrollITDeclaration updatedPayrollITDeclaration = payrollITDeclarationService
                .draftPreviousEmployee(planId, employeeTax);
        return GenericResponse.success(translator.toLocale(AppMessages.PREVIOUS_EMPLOYEE_UPDATED),
                updatedPayrollITDeclaration);
    }

    @PutMapping("/update/families")
    public GenericResponse<PayrollITDeclaration> updateFamilies(@RequestParam String planId,
            @RequestBody List<FamilyList> families) {

        log.info("Update Families in Payroll IT declaration");
        PayrollITDeclaration payrollITDeclaration = payrollITDeclarationService.updateFamilies(planId,
                families);
        return GenericResponse.success(translator.toLocale(AppMessages.FAMILY_UPDATED), payrollITDeclaration);
    }

}
