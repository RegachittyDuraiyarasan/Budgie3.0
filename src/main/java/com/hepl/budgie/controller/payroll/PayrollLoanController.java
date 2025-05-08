package com.hepl.budgie.controller.payroll;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.PayrollLoanDTO;
import com.hepl.budgie.entity.payroll.PayrollLoan;
import com.hepl.budgie.service.payroll.PayrollLoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Payroll Loan", description = "Create and Manage the Loan Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/loan")
public class PayrollLoanController {
    private final PayrollLoanService payrollLoanService;

    @GetMapping()
    @Operation(summary = "List the Loan")
    public GenericResponse<List<PayrollLoan>> fetch() {
        log.info("Payroll Component Fetched Successfully");
        return GenericResponse.success(payrollLoanService.fetch());
    }

    @PostMapping()
    @Operation(summary = "Add the Loan")
    public GenericResponse<String> add(@Valid @RequestBody PayrollLoanDTO request) {
        log.info("Payroll Component Added Successfully");
        return GenericResponse.success(payrollLoanService.add("save", request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update the Loan")
    public GenericResponse<String> update() {
        log.info("Payroll Component Updated Successfully");
        return GenericResponse.success(payrollLoanService.update());
    }

    @PutMapping("/status/{id}")
    @Operation(summary = "Status the Loan")
    public GenericResponse<String> status() {
        log.info("Payroll Component Status Successfully");
        return GenericResponse.success(payrollLoanService.status());
    }

    @PutMapping("/delete/{id}")
    @Operation(summary = "Delete the Loan")
    public GenericResponse<String> delete() {
        log.info("Payroll Component Deleted Successfully");
        return GenericResponse.success(payrollLoanService.delete());
    }
}
