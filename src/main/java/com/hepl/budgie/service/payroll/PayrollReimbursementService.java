package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.payroll.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface PayrollReimbursementService {
    Map<String, Object> addBill(AddReimbursementDTO request);

    List<ReimbursementBillDTO> listBills();

    String approveOrRejectReimbursementBills(String id, ReimbursementApprovedDTO request);

    String deleteBills(String id);

    String updateBill(String id, UpdateReimbursementDTO request) throws IOException;

    List<FbpCreatePlanDTO> createEmpList();

    String createReimbursementPlan(List<PayrollFBPCreatePlan> request);

    String extendedReimbursementPlan(PayrollFBPCreatePlan request);

    List<Map<String, Object>> getPendingReimbursementBills();
}
