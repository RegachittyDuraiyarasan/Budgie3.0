package com.hepl.budgie.controller.payroll;


import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PaySheetRunDTO;
import com.hepl.budgie.dto.payroll.PayrollPaysheetDTO;
import com.hepl.budgie.service.payroll.PayrollPaySheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@Tag(name = "Payroll Paysheet Controller", description = "Create and Manage the Payroll Paysheet Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/paysheet")
public class PayrollPaysheetController {

    private final PayrollPaySheetService payrollPaysheetService;

    @PostMapping("/hr/run")
    @Operation(summary = "Pay sheet Run ")
    public GenericResponse<List<PayrollPaysheetDTO>> runPaysheet(@RequestBody @Valid PaySheetRunDTO dto) throws ExecutionException, InterruptedException {
        log.info("Run Employee Paysheet -{}", dto.getType());
        return GenericResponse.success(payrollPaysheetService.runPaySheet(dto.getType()));
    }

    @GetMapping("/payroll-status")
    @Operation(summary = "Pay sheet Run ")
    public GenericResponse<Map<String, Object>> getPayrollStatus()  {
        return GenericResponse.success(payrollPaysheetService.getPayrollStatus());
    }
}
