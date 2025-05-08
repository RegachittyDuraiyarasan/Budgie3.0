package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.payroll.FbpCreatePlanDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPCreatePlan;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.*;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.*;
import com.hepl.budgie.service.payroll.PayrollFBPService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.PayrollMonthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollFBPServiceImpl implements PayrollFBPService {
    private final MongoTemplate mongoTemplate;
    private final PayrollFBPComponentRepository payrollFBPComponentRepository;
    private final PayrollFBPRangeRepository payrollFBPRangeRepository;
    private final PayrollFBPMasterRepository payrollFBPMasterRepository;
    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final PayrollFBPCreatePlanRepository payrollFBPCreatePlanRepository;
    private final PayrollCTCBreakupsRepository payrollCTCBreakupsRepository;
    private final JWTHelper jwtHelper;
    private final PayrollMonthProvider payrollMonthProvider;

    /* FBP Component List Implementation */
    @Override
    public List<PayrollFBPComponentMaster> list() {
        return payrollFBPComponentRepository.findByNonDeleteList(mongoTemplate, jwtHelper.getOrganizationCode());
    }

    /* FBP Component Add and Update Implementation */
    @Override
    public void add(String func, PayrollFBPComponentMaster request) {
        boolean check = false;
        request.setOrgId(jwtHelper.getOrganizationCode());
        log.info("Func Status - {}", func);
        if("save".equalsIgnoreCase(func)) {
            request.setComponentId(payrollFBPComponentRepository
                    .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                    .map(e-> AppUtils.generateUniqueId(e.getComponentId()))
                    .orElse("FBP00001"));
            check = payrollFBPComponentRepository.existsByComponentNameAndStatus("save", mongoTemplate, request);
        }else if("update".equalsIgnoreCase(func)) {
            check = payrollFBPComponentRepository.existsByComponentNameAndStatus("update", mongoTemplate, request);
        }
        log.info("Check Status - {}", check);

        if(check)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND);
        boolean result = payrollFBPComponentRepository.upsert(request, mongoTemplate);
        if(!result)
            throw new CustomResponseStatusException(AppMessages.COMPONENT_ID_NOT_FOUND, HttpStatus.BAD_REQUEST, new Object[]{request.getComponentId()});
    }

    @Override
    public boolean status(String id) {
        PayrollFBPComponentMaster component = payrollFBPComponentRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Toggle status efficiently
        String newStatus = component.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ?
                Status.INACTIVE.label :
                Status.ACTIVE.label;

        return payrollFBPComponentRepository.updateStatus(id, newStatus, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public boolean deleteStatus(String id) {
        PayrollFBPComponentMaster component = payrollFBPComponentRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Mark status as 'DELETED'
        return payrollFBPComponentRepository.updateStatus(id, Status.DELETED.label, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    /* FBP Range List Implementation */
    @Override
    public List<PayrollFBPRange> listRange() {
        return payrollFBPRangeRepository.findByNonDeleteList(mongoTemplate,jwtHelper.getOrganizationCode());
    }

    /* FBP Range Add Implementation */
    @Override
    public void range(String func, PayrollFBPRange request) {
        boolean check = false;
        request.setOrgId(jwtHelper.getOrganizationCode());
        log.info("Func Status - {}", func);
        log.info("FBP Range Details - {}",request);

        if("save".equalsIgnoreCase(func)) {
            request.setRangeId(payrollFBPRangeRepository
                    .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                    .map(e-> AppUtils.generateUniqueId(e.getRangeId()))
                    .orElse("FBP-R00001"));
            check = payrollFBPRangeRepository.existsByRange("save", mongoTemplate, request, jwtHelper.getOrganizationCode());
        }else if("update".equalsIgnoreCase(func)) {
            check = payrollFBPRangeRepository.existsByRange("update", mongoTemplate, request, jwtHelper.getOrganizationCode());
        }
        log.info("Check the range - {}", check);
        if(check)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND);
        boolean result = payrollFBPRangeRepository.upsert(request, mongoTemplate, jwtHelper.getOrganizationCode());
        if(!result)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "FBP Component ID doesn't match : " + request.getRangeId());
    }

    @Override
    public boolean statusRange(String id) {
        PayrollFBPRange component = payrollFBPRangeRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Toggle status efficiently
        String newStatus = component.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ?
                Status.INACTIVE.label :
                Status.ACTIVE.label;

        return payrollFBPRangeRepository.updateStatus(id, newStatus, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public boolean deleteStatusRange(String id) {
        PayrollFBPRange component = payrollFBPRangeRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Mark status as 'DELETED'
        return payrollFBPRangeRepository.updateStatus(id, Status.DELETED.label, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    /* FBP Master List Implementation */
    @Override
    public List<PayrollFBPMaster> listMaster(String id) {
        List<PayrollFBPMaster> list = payrollFBPMasterRepository
                .findByNonDeleteList(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_COMPONENT_RANGE_NOT_FOUND));
        if(list.isEmpty())
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_COMPONENT_RANGE_NOT_FOUND);
        log.info("List of Components - {}", list);
        return list;
    }

    /* FBP Master Add Implementation */
    @Override
    public void addFBPMaster(String func, List<PayrollFBPMaster> request) {
        List<Map<String, String>> errorList = new ArrayList<>();

        for (PayrollFBPMaster payrollFBPMaster : request) {
            Map<String, String> error = new HashMap<>();
            try {
                payrollFBPMaster.setOrgId(jwtHelper.getOrganizationCode());
                if (!payrollFBPRangeRepository.existsByRangeIdActive(payrollFBPMaster.getRangeId(), mongoTemplate, jwtHelper.getOrganizationCode())) {
                    error.put("rangeId", "FBP Range Component is not matched");
                }
                if (payrollFBPComponentRepository.existsByComponentNameActive(payrollFBPMaster.getFbpType(), mongoTemplate, jwtHelper.getOrganizationCode()).isEmpty()) {
                    error.put("fbpType", "FBP Component is not matched");
                }
                if (!error.isEmpty()) {
                    errorList.add(error);
                    continue;
                }
                if ("save".equalsIgnoreCase(func)) {
                    payrollFBPMaster.setFbpId(payrollFBPMasterRepository
                            .findLatestComponent(mongoTemplate)
                            .map(e -> AppUtils.generateUniqueId(e.getFbpId()))
                            .orElse("FBP-M00001"));
                }
                Optional<PayrollFBPMaster> check = payrollFBPMasterRepository.findByRangeIdandFbpMaster(mongoTemplate, payrollFBPMaster);
                log.info("FBP Master Check - {}", check);
                if (check.isPresent()) {
                    PayrollFBPMaster existingMaster = check.get();
                    if (existingMaster.getFbpType().equalsIgnoreCase(payrollFBPMaster.getFbpType())) {
                        error.put("fbpType", "Already Exists");
                    }
                    if (!error.isEmpty()) {
                        errorList.add(error);
                        continue;
                    }
                }
                payrollFBPMasterRepository.upsert(payrollFBPMaster, mongoTemplate);
            } catch (Exception e) {
                error.put("error", "Unexpected error: " + e.getMessage());
            }
            if (!error.isEmpty()) {
                errorList.add(error);
            }
        }
        if (!errorList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorList.toString());
        }
    }

    /* FBP Master Update Implementation */
    @Override
    public boolean updateFBPMaster(PayrollFBPMaster request) {
        request.setOrgId(jwtHelper.getOrganizationCode());
        Map<String, String> errorList = new HashMap<>();
        log.info("Request Info : {}", request);
        Optional<PayrollFBPMaster> check = payrollFBPMasterRepository.findByRangeIdandFbpMaster(mongoTemplate, request);

        if (check.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "FBP Master record not found for update");
        }

        PayrollFBPMaster existingMaster = check.get();
        log.info("Existing Info : {}", existingMaster);

        if (payrollFBPComponentRepository.existsByComponentNameActive(request.getFbpType(), mongoTemplate, jwtHelper.getOrganizationCode()).isEmpty()) {
            errorList.put("fbpType", "FBP Type doesn't matched in FBP Component");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorList.toString());
        }

        if (existingMaster.getFbpType().equalsIgnoreCase(request.getFbpType())) {
            errorList.put("fbpType", "FBP Type already exists");
            if (existingMaster.getPayType().equalsIgnoreCase(request.getPayType())) {
                errorList.put("payType", "Pay Type already exists");
            }
            if (existingMaster.getAmount() == request.getAmount()) {
                errorList.put("amount", "Amount already exists");
            }
        }

        if (!errorList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, errorList.toString());
        }

        // Update only if the request contains new values
        if (request.getFbpType() != null && !request.getFbpType().isEmpty()) {
            existingMaster.setFbpType(request.getFbpType());
        }
        if (request.getPayType() != null && !request.getPayType().isEmpty()) {
            existingMaster.setPayType(request.getPayType());
        }
        if (request.getAmount() != 0) {
            existingMaster.setAmount(request.getAmount());
        }

        return payrollFBPMasterRepository.update(mongoTemplate, existingMaster);
    }

    /* FBP Master Status Implementation */
    @Override
    public boolean statusMaster(String id) {
        PayrollFBPMaster component = payrollFBPMasterRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Toggle status efficiently
        String newStatus = component.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ?
                Status.INACTIVE.label :
                Status.ACTIVE.label;

        return payrollFBPMasterRepository.updateStatus(id, newStatus, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    /* FBP Master Delete Implementation */
    @Override
    public boolean deleteStatusMaster(String id) {
        PayrollFBPMaster component = payrollFBPMasterRepository
                .findByComponentId(id, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ID_NOT_FOUND));

        // Mark status as 'DELETED'
        return payrollFBPMasterRepository.updateStatus(id, Status.DELETED.label, mongoTemplate, jwtHelper.getOrganizationCode());
    }

    /* ---------------- HR FBP PLAN CREATED -------------------- */
    @Override
    public void createPlan(List<PayrollFBPCreatePlan> request) {
        PayrollLockMonth payrollLockMonth = payrollLockMonthRepository.getLockedPayrollMonths(mongoTemplate, jwtHelper.getOrganizationCode(), "IN");

        PayrollMonth payrollMonth = payrollMonthProvider.getPayrollMonth();
        log.info("Lock month : {}", payrollLockMonth);
        log.info("Current Payroll Month : {}", payrollMonth);

        for (PayrollFBPCreatePlan plan : request) {
            processPayrollPlan(plan, payrollMonth);
        }
    }

    private void processPayrollPlan(PayrollFBPCreatePlan plan, PayrollMonth payrollMonth) {
        Optional<PayrollFBPPlan> exist = payrollFBPCreatePlanRepository.findByEmpIdAndFinYear(plan.getEmpId(),payrollMonth.getFinYear(), mongoTemplate, jwtHelper.getOrganizationCode());
        PayrollFBPPlan payrollFBPPlan = exist.orElseGet(PayrollFBPPlan::new);
        if(exist.isEmpty()) {
            payrollFBPPlan.setFbpPlanId(payrollFBPCreatePlanRepository
                    .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                    .map(e-> AppUtils.generateUniqueId(e.getFbpPlanId()))
                    .orElse("PLAN00001"));
        }
        payrollFBPPlan.setEmpId(plan.getEmpId());
        LocalDate startDate = plan.getEndDate();
        payrollFBPPlan.setEndDate(startDate);

        if (exist.isPresent()) {
            payrollFBPPlan.setStatus(DataOperations.DRAFT.label);
            payrollFBPCreatePlanRepository.updateFBPPlan(plan.getEmpId(), payrollMonth.getFinYear(), payrollFBPPlan, mongoTemplate, jwtHelper.getOrganizationCode());
        } else {
            payrollFBPPlan.setStatus(DataOperations.CREATED.label);
            payrollFBPPlan.setFinancialYear(payrollMonth.getFinYear());
            payrollFBPCreatePlanRepository.savePlan(payrollFBPPlan, mongoTemplate, jwtHelper.getOrganizationCode());
        }
    }

    @Override
    public List<FbpCreatePlanDTO> listPlan() {
        return payrollFBPCreatePlanRepository.findByStatus(DataOperations.SUBMIT.label, mongoTemplate,payrollMonthProvider.getPayrollMonth().getFinYear(), jwtHelper.getOrganizationCode());
    }

    @Override
    public void considerPlan(List<FbpCreatePlanDTO> empIds) {
        for(FbpCreatePlanDTO plan: empIds) {
            Optional<PayrollFBPPlan> exist = payrollFBPCreatePlanRepository.findByPlanIdAndEmployeeAndFinancialYear("2024-2025", plan, mongoTemplate, jwtHelper.getOrganizationCode());
            if(exist.isPresent()) {
                payrollFBPCreatePlanRepository.updateEmployeeConsiderPlan(DataOperations.CONSIDER.label, plan, "2024-2025",mongoTemplate, jwtHelper.getOrganizationCode());
            }else {
                log.info("Not Existing : {}",exist);
            }
        }
    }

    @Override
    public List<EmployeeActiveDTO> employeeList() {
        return payrollCTCBreakupsRepository.employeeList(mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public List<EmployeeActiveDTO> activeEmployeeList(){
        return payrollCTCBreakupsRepository.activeEmployeeList(mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public List<EmployeeActiveDTO> considerEmployeeList(){
        return payrollCTCBreakupsRepository.considerEmployeeList(mongoTemplate, jwtHelper.getOrganizationCode());
    }
}
