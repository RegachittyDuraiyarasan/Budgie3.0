package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.service.payroll.PayrollPayslipService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payroll Payslip", description = "Create and Manage the Payslip Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/payroll/payslip")
public class PayrollPayslipController {

    private final Translator translator;
    private final PayrollPayslipService payrollPayslipService;
    public PayrollPayslipController(Translator translator, PayrollPayslipService payrollPayslipService) {
        this.translator = translator;
        this.payrollPayslipService = payrollPayslipService;
    }

    @GetMapping
    public GenericResponse<String> getPayslipEmployeeList() {
        payrollPayslipService.getPayslipEmployeeList();
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] {"Change the content"}));
    }
}
