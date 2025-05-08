package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.AttendanceDateDTO;
import com.hepl.budgie.dto.payroll.AttendanceDateFetchDTO;
import com.hepl.budgie.dto.payroll.PayrollMonthDTO;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.service.payroll.PayrollLockMonthService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll Lock Month", description = "Create and Manage the Lock-Month Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/lock-month")
public class PayrollLockMonthController {
    private final PayrollLockMonthService payrollLockMonthService;
    private final Translator translator;

    @PostMapping("/attendance-date")
    public GenericResponse<String> attendanceDate(@Valid @RequestBody AttendanceDateDTO request) {
        payrollLockMonthService.attendanceDate(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "Lock Month" }));
    }

    @PostMapping()
    public GenericResponse<String> generateMonth(@RequestParam String startDate) {
        payrollLockMonthService.generateMonth(startDate);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "Lock Month" }));
    }

    @PutMapping("/{payrollMonth}")
    public GenericResponse<String> updateLockMonth(@PathVariable String payrollMonth) {
        payrollLockMonthService.updateLockMonth(payrollMonth);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] { "Lock Month" }));
    }

    @GetMapping("/standardDate")
    public GenericResponse<AttendanceDateFetchDTO> standardDate() {
        return GenericResponse.success(payrollLockMonthService.standardDate());
    }
    @GetMapping("/payrollMonth")
    public GenericResponse<List<PayrollMonthDTO>> listPayrollMonth() {
        return GenericResponse.success(payrollLockMonthService.listPayrollMonth());
    }


}
