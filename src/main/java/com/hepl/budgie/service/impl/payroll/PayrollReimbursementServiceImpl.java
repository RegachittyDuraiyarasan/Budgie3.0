package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.*;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.payroll.PayrollReimbursementClaim;
import com.hepl.budgie.entity.payroll.ReimbursementBill;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.PayrollFBPCreatePlanRepository;
import com.hepl.budgie.repository.payroll.PayrollReimbursementClaimRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.payroll.PayrollReimbursementService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.hepl.budgie.utils.PayrollMonthProvider;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollReimbursementServiceImpl implements PayrollReimbursementService {

    private final PayrollMonthProvider payrollMonthProvider;
    private final PayrollReimbursementClaimRepository payrollReimbursementClaimRepository;
    private final PayrollFBPCreatePlanRepository payrollFBPCreatePlanRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final FileService fileService;

    @Override
    public List<FbpCreatePlanDTO> createEmpList() {
        return payrollFBPCreatePlanRepository.findByStatus(DataOperations.CONSIDER.label, mongoTemplate,payrollMonthProvider.getPayrollMonth().getFinYear(), jwtHelper.getOrganizationCode());
    }

    @Override
    public String createReimbursementPlan(List<PayrollFBPCreatePlan> request) {
        log.info("Reimbursement create plan : {}", request);
        BulkWriteResult result = payrollReimbursementClaimRepository.bulkUpsert(request, payrollMonthProvider.getPayrollMonth(), mongoTemplate, jwtHelper.getOrganizationCode());
        log.info("Updated Results : {}",result.getInserts());
        return "Created Successfully";
    }

    @Override
    public String extendedReimbursementPlan(PayrollFBPCreatePlan request) {
        PayrollReimbursementClaim exist = payrollReimbursementClaimRepository.existsBills(mongoTemplate, request.getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode())
                .orElseThrow(() ->  new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.REIMBURSEMENT_NOT_FOUND));
        exist.setEndDate(AppUtils.parseLocalDate(request.getEndDate(), LocaleContextHolder.getTimeZone().getID()));

        boolean result = payrollReimbursementClaimRepository.updateExtendedDate(exist, mongoTemplate, payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode());
        return result ? "Update Successfully" : "Can't update the end date";
    }

    @Override
    public List<ReimbursementBillDTO> listBills() {
        log.info("org id : {}", jwtHelper.getOrganizationCode());
        log.info("emp id : {}", jwtHelper.getUserRefDetail().getEmpId());
        return payrollReimbursementClaimRepository.listBills(jwtHelper.getUserRefDetail().getEmpId(),mongoTemplate,payrollMonthProvider.getPayrollMonth().getFinYear(), jwtHelper.getOrganizationCode());
    }

    @Override
    public Map<String, Object> addBill(AddReimbursementDTO request) {
        PayrollMonth payrollMonth = payrollMonthProvider.getPayrollMonth();
        String empId = jwtHelper.getUserRefDetail().getEmpId();

        PayrollReimbursementClaim claim = payrollReimbursementClaimRepository
                .findByPayrollMonthAndEmpId(payrollMonth.getPayrollMonth(), empId, payrollMonth.getFinYear(),
                        jwtHelper.getOrganizationCode(), mongoTemplate)
                .orElseGet(() -> initializeNewClaim(payrollMonth, AppUtils.parseLocalDate(LocalDate.now().plusDays(2), LocaleContextHolder.getTimeZone().getID()), empId));

        log.info("Claim for test -{}", claim);

        List<String> successfulBills = Collections.synchronizedList(new ArrayList<>());
        Map<Integer, Map<String, String>> failedBills = new ConcurrentHashMap<>();
        Set<String> seenBillNumbers = Collections.synchronizedSet(new HashSet<>());
        Set<String> failedFbpTypes = Collections.synchronizedSet(new HashSet<>());

        for (int index = 0; index < request.getBills().size(); index++) {
            AddReimbursementDTO.BillData bill = request.getBills().get(index);
            try {
                if (!seenBillNumbers.add(bill.getBillNo())) {
                    throw new RuntimeException("Duplicate billNo in request");
                }

                if (processReimbursementBill(bill, claim)) {
                    successfulBills.add("Index " + index + ": Saved successfully");
                    payrollReimbursementClaimRepository.saveReimbursementBill(claim, mongoTemplate, jwtHelper.getOrganizationCode());
                }
            } catch (Exception e) {
                failedFbpTypes.add(bill.getFbpType());
                Map<String, String> errorDetails = new HashMap<>();
                errorDetails.put("billNo", bill.getBillNo());
                errorDetails.put("fbpType", bill.getFbpType());
                errorDetails.put("error", e.getMessage());
                failedBills.put(index, errorDetails);
            }
        }

//        claim.getReimbursement().removeIf(reimbursement -> failedFbpTypes.contains(reimbursement.getFbpType()));
//        payrollReimbursementClaimRepository.saveReimbursementBill(claim, mongoTemplate, jwtHelper.getOrganizationCode());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", successfulBills.isEmpty() ? "All operations failed" :
                failedBills.isEmpty() ? "All operations successful" : "Operation completed with some errors");

        if (!failedBills.isEmpty()) {
            response.put("bills", failedBills);
        }

        return response;
    }

    private PayrollReimbursementClaim initializeNewClaim(PayrollMonth payrollMonth, ZonedDateTime date, String empId) {
        PayrollReimbursementClaim exist = payrollReimbursementClaimRepository.existsBills(mongoTemplate, empId, payrollMonth, jwtHelper.getOrganizationCode()).orElse(new PayrollReimbursementClaim());
        exist.setPayrollMonth(payrollMonth.getPayrollMonth());
        exist.setFinYear(payrollMonth.getFinYear());
        exist.setEmpId(empId);
        exist.setEndDate(date);
        exist.setReimbursement(new ArrayList<>());
        return exist;
    }

    private boolean processReimbursementBill(AddReimbursementDTO.BillData bill, PayrollReimbursementClaim claim) throws IOException {
        if (bill.getAttachment() == null || bill.getAttachment().isEmpty()) {
            throw new RuntimeException("Attachment is required");
        }

        PayrollReimbursementClaim.Reimbursement reimbursement = claim.getReimbursement().stream()
                .filter(r -> r.getFbpType().equals(bill.getFbpType()))
                .findFirst()
                .orElseGet(() -> createNewReimbursement(claim.getReimbursement(), bill.getFbpType()));

        if (reimbursement.getReimbursementBills().stream().anyMatch(existingBill -> existingBill.getBillNo().equalsIgnoreCase(bill.getBillNo()))) {
            throw new RuntimeException("Bill number already exists");
        }

        addReimbursementBill(reimbursement, bill);
        return true;
    }

    private PayrollReimbursementClaim.Reimbursement createNewReimbursement(List<PayrollReimbursementClaim.Reimbursement> reimbursementList, String fbpType) {
        PayrollReimbursementClaim.Reimbursement reimbursement = new PayrollReimbursementClaim.Reimbursement();
        reimbursement.setFbpType(fbpType);
        reimbursement.setReimbursementBills(Collections.synchronizedList(new ArrayList<>()));
        reimbursementList.add(reimbursement);
        return reimbursement;
    }

    private void addReimbursementBill(PayrollReimbursementClaim.Reimbursement reimbursement, AddReimbursementDTO.BillData bill) throws IOException {
        ReimbursementBill reimbursementBill = new ReimbursementBill();
        reimbursementBill.setClaimDate(LocalDate.now().toString());
        reimbursementBill.setBillDate(bill.getBillDate());
        reimbursementBill.setBillNo(bill.getBillNo());
        reimbursementBill.setAttachment(uploadFile(bill.getAttachment(), jwtHelper.getUserRefDetail().getEmpId()));
        reimbursementBill.setBillAmount(Double.parseDouble(bill.getBillAmount()));
        reimbursementBill.setStatus(DataOperations.PENDING.label);

        synchronized (reimbursement.getReimbursementBills()) {
            reimbursement.getReimbursementBills().add(reimbursementBill);
        }
        reimbursement.setTypeTotal(reimbursement.getTypeTotal() + reimbursementBill.getBillAmount());
    }

    private ReimbursementBill.ReimbursementDocument uploadFile(MultipartFile file, String empId) throws IOException {
        String folderName = FileType.REIMBURSEMENT.folderName;
        String fileName = empId + "_" + folderName;
        String uploadedFileName = fileService.uploadFile(file, FileType.REIMBURSEMENT, fileName);
        return new ReimbursementBill.ReimbursementDocument(folderName, uploadedFileName);
    }

    @Override
    public String updateBill(String id, UpdateReimbursementDTO request) throws IOException {
        if(payrollReimbursementClaimRepository.existsBills(id, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.REIMBURSEMENT_NOT_FOUND);
        }
        if (payrollReimbursementClaimRepository.existsBillNo(id, request, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.REIMBURSEMENT_NOT_FOUND);
        }
        ReimbursementBill.ReimbursementDocument document = uploadFile(request.getAttachment(), jwtHelper.getUserRefDetail().getEmpId());

        payrollReimbursementClaimRepository.updateBills(id, request, document, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode());
        return "Update Successfully";
    }

    @Override
    public String approveOrRejectReimbursementBills(String id, ReimbursementApprovedDTO request) {
        if(payrollReimbursementClaimRepository.existsBills(id, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.REIMBURSEMENT_NOT_FOUND);
        }
        payrollReimbursementClaimRepository.approveOrRejectReimbursementBills(id, request, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode());
        return "Updated Successfully";
    }

    @Override
    public String deleteBills(String id) {
        if(payrollReimbursementClaimRepository.existsBills(id, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.REIMBURSEMENT_NOT_FOUND);
        }
        payrollReimbursementClaimRepository.deleteBills(id, mongoTemplate, jwtHelper.getUserRefDetail().getEmpId(), payrollMonthProvider.getPayrollMonth(), jwtHelper.getOrganizationCode());
        return "Deleted Successfully";
    }

    @Override
    public List<Map<String, Object>> getPendingReimbursementBills() {
        
        log.info("Get Pending Reimbursement Bills: {}");
        String orgId = jwtHelper.getOrganizationCode();
        return payrollReimbursementClaimRepository.getPendingReimbursementBills(mongoTemplate, orgId);
    }

}
