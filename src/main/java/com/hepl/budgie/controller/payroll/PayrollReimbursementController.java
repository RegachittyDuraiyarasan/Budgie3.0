package com.hepl.budgie.controller.payroll;


import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.payroll.*;
import com.hepl.budgie.service.payroll.PayrollReimbursementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Tag(name = "Payroll Reimbursement Employee Controller", description = "Create and Manage the Payroll Reimbursement Employee Controller")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/payroll/reimbursement")
public class PayrollReimbursementController {

    private final PayrollReimbursementService payrollReimbursementService;

    @GetMapping("/hr/employee/list")
    @Operation(summary = "List reimbursement for employee")
    public GenericResponse<List<FbpCreatePlanDTO>> createEmpList() {
        log.info("List reimbursement Employee");
        return GenericResponse.success(payrollReimbursementService.createEmpList());
    }

    @PostMapping("/hr/create/plan")
    @Operation(summary = "List reimbursement for employee")
    public GenericResponse<String> createReimbursementPlan(@Valid @RequestBody List<PayrollFBPCreatePlan> request) {
        log.info("Create Plan reimbursement");
        return GenericResponse.success(payrollReimbursementService.createReimbursementPlan(request));
    }

    @PostMapping("/hr/extended")
    @Operation(summary = "Extended reimbursement for employee")
    public GenericResponse<String> extendedReimbursementPlan(@RequestBody PayrollFBPCreatePlan request) {
        log.info("Extended Plan reimbursement");
        return GenericResponse.success(payrollReimbursementService.extendedReimbursementPlan(request));
    }

    @GetMapping("/emp")
    @Operation(summary = "List reimbursement bill for employee")
    public GenericResponse<List<ReimbursementBillDTO>> listReimbursement() {
        log.info("List reimbursement bills");
        return GenericResponse.success(payrollReimbursementService.listBills());
    }
    @PostMapping("/emp")
    @Operation(summary = "Add reimbursement bill for employee")
    public GenericResponse<Map<String, Object>> addReimbursement(@Valid @ModelAttribute AddReimbursementDTO request) {
        log.info("Received {} reimbursement bills", request);
        return GenericResponse.success(payrollReimbursementService.addBill(request));
    }

    @PutMapping("/emp/{id}")
    @Operation(summary = "update reimbursement bill for employee")
    public GenericResponse<String> updateReimbursementBills(@PathVariable String id, @Valid @ModelAttribute UpdateReimbursementDTO request) throws IOException {
        log.info("Received Id: {}, Data: {} reimbursement bills", id, request);
        return GenericResponse.success(payrollReimbursementService.updateBill(id, request));
    }
    @PutMapping("/emp/approveOrReject/{id}")
    @Operation(summary = "approve or reject reimbursement bill for employee")
    public GenericResponse<String> approveOrRejectReimbursementBills(@PathVariable String id, @Valid @RequestBody ReimbursementApprovedDTO request) {
        log.info("Received Id: {}, Data: {} reimbursement bills", id, request);
        return GenericResponse.success(payrollReimbursementService.approveOrRejectReimbursementBills(id, request));
    }

    @DeleteMapping("/emp/deleteBill/{id}")
    @Operation(summary = "hard delete reimbursement bill for employee")
    public GenericResponse<String> deleteReimbursementBills(@PathVariable String id) {
        log.info("Received Id: {}", id);
        return GenericResponse.success(payrollReimbursementService.deleteBills(id));
    }

    @GetMapping("/emp/pending-list")
    public GenericResponse<List<Map<String, Object>>> getPendingReimbursementBills() {

        log.info("Get pending reimbursement bills");
        List<Map<String, Object>> payrollReimbursementClaims = payrollReimbursementService.getPendingReimbursementBills();
        return GenericResponse.success(payrollReimbursementClaims);
    }

}
