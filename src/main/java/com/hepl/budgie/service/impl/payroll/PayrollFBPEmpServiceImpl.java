package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollFBPEmpIndexDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPEmpListDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPSaveDTO;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.entity.payroll.*;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.payroll.*;
import com.hepl.budgie.service.payroll.PayrollFBPEmpService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.PayrollMonthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollFBPEmpServiceImpl implements PayrollFBPEmpService {
    private final PayrollFBPMasterRepository payrollFBPMasterRepository;
    private final PayrollFBPComponentRepository payrollFBPComponentRepository;
    private final PayrollFBPCreatePlanRepository payrollFBPCreatePlanRepository;
    private final PayrollCTCBreakupsRepository payrollCTCBreakupsRepository;
    private final PayrollFBPRangeRepository payrollFBPRangeRepository;
    private final PayrollMonthProvider payrollMonthProvider;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final Translator translator;

    @Override
    public PayrollFBPEmpIndexDTO index() {
        PayrollCTCBreakups ctcBreakups = payrollCTCBreakupsRepository.findByEmpIdAndRevisionOrderDesc(jwtHelper.getUserRefDetail().getEmpId(), mongoTemplate, jwtHelper.getOrganizationCode()).orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.CTC_NOT_FOUND));
        PayrollFBPRange range = payrollFBPRangeRepository
                .findBySalaryRange(ctcBreakups.getGrossEarnings(), mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_RANGE_NOT_FOUND));

        List<PayrollFBPMaster> fbpComponent = payrollFBPMasterRepository.existsByFBPMaster(mongoTemplate,range.getRangeId(),jwtHelper.getOrganizationCode());
        Optional<PayrollFBPPlan> exist = payrollFBPCreatePlanRepository.findByEmpIdAndFinYear(jwtHelper.getUserRefDetail().getEmpId(),"2024-2025", mongoTemplate, jwtHelper.getOrganizationCode());

        PayrollFBPEmpIndexDTO fbpEmp = new PayrollFBPEmpIndexDTO();
        fbpEmp.setFbpComponent(fbpComponent);
        fbpEmp.setSpecialAllowance(ctcBreakups.getEarningColumns().getOrDefault("Special Allowance", 0));
        if(exist.isPresent()) {
            if(exist.get().getStatus().equalsIgnoreCase(DataOperations.SUBMIT.label)) {
                fbpEmp.setStatus(true);
            }
        }
        return fbpEmp;
    }

    // @Override
    // public List<PayrollFBPEmpListDTO> fbpList() {
    //     return Collections.<PayrollFBPEmpListDTO>emptyList();
//        return payrollFBPCreatePlanRepository
//                .findByEmpIdAndFinYear(jwtHelper.getUserRefDetail().getEmpId(), "2024-2025", mongoTemplate, jwtHelper.getOrganizationCode())
//                .map(fbpPlan -> {
//                    if (fbpPlan.getMonthlyAmount() == null || fbpPlan.getMonthlyAmount().isEmpty()) {
//                        return Collections.<PayrollFBPEmpListDTO>emptyList();
//                    }
//
//                    List<PayrollFBPEmpListDTO> listDTOS = new ArrayList<>();
//                    fbpPlan.getMonthlyAmount().forEach((key, monthlyAmount) -> {
//                        PayrollFBPEmpListDTO fbpEmpList = new PayrollFBPEmpListDTO();
//                        Optional<PayrollFBPComponentMaster> component = payrollFBPComponentRepository.existsByComponentNameActive(key, mongoTemplate, jwtHelper.getOrganizationCode());
//                        if(component.isPresent()) {
//                            fbpEmpList.setFbpId(component.get().getComponentId());
//                            fbpEmpList.setFbpType(key);
//                            fbpEmpList.setMaxAmount(fbpPlan.getMaxAmount().getOrDefault(key, 0));
//                            fbpEmpList.setMonthlyAmount(monthlyAmount);
//                            fbpEmpList.setYearlyAmount(fbpPlan.getYearlyAmount().getOrDefault(key, 0));
//                            fbpEmpList.setPayrollMonth(fbpPlan.getPayrollMonth().getOrDefault(key, null));
//                        }
//
//                        listDTOS.add(fbpEmpList);
//                    });
//                    return listDTOS;
//                })
//                .orElse(Collections.emptyList());
    // }
    @Override
    public List<PayrollFBPEmpListDTO> fbpList() {
        Optional<List<PayrollFBPEmpListDTO>> optionalList = payrollFBPCreatePlanRepository
            .findByEmpIdAndFinYear(jwtHelper.getUserRefDetail().getEmpId(), "2024-2025", mongoTemplate, jwtHelper.getOrganizationCode())
            .flatMap(fbpPlan -> {
                if (fbpPlan.getFbp() == null || fbpPlan.getFbp().isEmpty()) {
                    return Optional.of(Collections.emptyList());
                }
    
                List<PayrollFBPEmpListDTO> listDTOS = new ArrayList<>();
                for (PayrollFBPPlan.Fbp fbp : fbpPlan.getFbp()) {
                    Optional<PayrollFBPComponentMaster> component = payrollFBPComponentRepository.existsByComponentNameActive(fbp.getFbpType(), mongoTemplate, jwtHelper.getOrganizationCode());
                    component.ifPresent(comp -> {
                        PayrollFBPEmpListDTO fbpEmpList = new PayrollFBPEmpListDTO();
                        fbpEmpList.setFbpId(comp.getComponentId());
                        fbpEmpList.setFbpType(fbp.getFbpType());
                        fbpEmpList.setMaxAmount(fbp.getMaxAmount());
                        fbpEmpList.setMonthlyAmount(fbp.getMonthlyAmount());
                        fbpEmpList.setYearlyAmount(fbp.getYearlyAmount());
                        fbpEmpList.setPayrollMonth(fbp.getPayrollMonth());
                        listDTOS.add(fbpEmpList);
                    });
                }
                System.out.println("__values___" + listDTOS);
                return Optional.of(listDTOS);
            });
    
        if (optionalList.isPresent()) {
            return optionalList.get();
        } else {
            System.out.println("__NotValues___");
            return Collections.emptyList();
        }
    }

//    @Override
//    public void fbpAdd(PayrollFBPSaveDTO request) {
//        String empId = jwtHelper.getUserRefDetail().getEmpId();
//        Optional<PayrollFBPPlan> exist = payrollFBPCreatePlanRepository.findByEmpIdAndFinYear(empId,"2024-2025", mongoTemplate, jwtHelper.getOrganizationCode());
//        if(exist.isPresent()) {
//            if(exist.get().getStatus().equalsIgnoreCase(DataOperations.SUBMIT.label)) {
//                throw new ResponseStatusException(HttpStatus.CONFLICT, "Already FBP Submit for ID");
//            }
//        }else {
//            throw new ResponseStatusException(HttpStatus.NO_CONTENT, "Need to HR release FBP Plan");
//        }
//        PayrollCTCBreakups ctcBreakups = payrollCTCBreakupsRepository
//                .findByEmpIdAndRevisionOrderDesc(empId, mongoTemplate, jwtHelper.getOrganizationCode())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT, "No CTC Breakups"));
//
//        PayrollFBPRange range = payrollFBPRangeRepository
//                .findBySalaryRange(ctcBreakups.getGrossEarnings(), mongoTemplate, jwtHelper.getOrganizationCode())
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NO_CONTENT, "Range not matched"));
//
//        List<PayrollFBPMaster> fbpList = payrollFBPMasterRepository
//                .existsByFBPMaster(mongoTemplate, range.getRangeId(), jwtHelper.getOrganizationCode());
//
//        Map<String, PayrollFBPMaster> fbpMasterMap = fbpList.stream()
//                .collect(Collectors.toMap(PayrollFBPMaster::getFbpType, Function.identity()));
//
//        PayrollFBPPlan empPlan = new PayrollFBPPlan();
//        List<Map<String, String>> errorList = new ArrayList<>();
//
//        Map<String, Integer> maxAmount = new HashMap<>();
//        Map<String, Integer> monthlyAmount = new HashMap<>();
//        Map<String, Integer> yearlyAmount = new HashMap<>();
//        Map<String, Integer> fbpStatus = new HashMap<>();
//        Map<String, String> payrollMonth = new HashMap<>();
//
//        for (PayrollFBPEmpListDTO fbp : Optional.ofNullable(request.getFbpList()).orElse(Collections.emptyList())) {
//            PayrollFBPMaster matchedFbpMaster = fbpMasterMap.get(fbp.getFbpType());
//            Map<String, String> error = new HashMap<>();
//
//            if (matchedFbpMaster == null) {
//                error.put(fbp.getFbpType(), "You are not in the range");
//            } else {
//                int requestedAmount = Optional.of(fbp.getMonthlyAmount()).orElse(0);
//                int maxAllowedAmount = matchedFbpMaster.getAmount();
//
//                if (requestedAmount > maxAllowedAmount) {
//                    error.put(fbp.getFbpType(), "Amount Exceeds");
//                } else {
//                    maxAmount.put(fbp.getFbpType(), maxAllowedAmount);
//                    monthlyAmount.put(fbp.getFbpType(), requestedAmount);
//                    yearlyAmount.put(fbp.getFbpType(), requestedAmount * 12);
//                    fbpStatus.put(fbp.getFbpType(), "save".equalsIgnoreCase(request.getStatus()) ? 1 : 0);
//                    payrollMonth.put(fbp.getFbpType(), "04-2025");
//                }
//            }
//
//            if (!error.isEmpty()) {
//                errorList.add(error);
//            }
//        }
//
//        empPlan.setMaxAmount(maxAmount);
//        empPlan.setMonthlyAmount(monthlyAmount);
//        empPlan.setYearlyAmount(yearlyAmount);
//        empPlan.setFbpStatus(fbpStatus);
//        empPlan.setPayrollMonth(payrollMonth);
//        empPlan.setStatus("save".equalsIgnoreCase(request.getStatus()) ? DataOperations.SUBMIT.label : DataOperations.DRAFT.label);
//
//        if(!errorList.isEmpty())
//            throw new ResponseStatusException(HttpStatus.CONFLICT, errorList.toString());
//
//        boolean update = payrollFBPCreatePlanRepository.updateEmpPlan(empId, "2024-2025" ,empPlan, mongoTemplate, jwtHelper.getOrganizationCode());
//        log.info("Updated Success :{}", update);
//    }

    @Override
    public void fbpAdd(PayrollFBPSaveDTO request) {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        PayrollMonth payroll = payrollMonthProvider.getPayrollMonth();
        String financialYear = payroll.getFinYear();

        Optional<PayrollFBPPlan> existOpt = payrollFBPCreatePlanRepository.findByEmpIdAndFinYear(
                empId, financialYear, mongoTemplate, jwtHelper.getOrganizationCode()
        );

        PayrollFBPPlan empPlan;
        List<PayrollFBPPlan.Fbp> existingFbpList = new ArrayList<>();

        if (existOpt.isPresent()) {
            empPlan = existOpt.get();

            if (empPlan.getStatus().equalsIgnoreCase(DataOperations.SUBMIT.label)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_ALREADY_DECLARED);
            }

            existingFbpList = empPlan.getFbp() != null ? empPlan.getFbp() : new ArrayList<>();
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_HR_RELEASE);
        }

        // Retrieve necessary salary details
        PayrollCTCBreakups ctcBreakups = payrollCTCBreakupsRepository
                .findByEmpIdAndRevisionOrderDesc(empId, mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.CTC_NOT_FOUND));

        PayrollFBPRange range = payrollFBPRangeRepository
                .findBySalaryRange(ctcBreakups.getGrossEarnings(), mongoTemplate, jwtHelper.getOrganizationCode())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FBP_RANGE_NOT_FOUND));

        List<PayrollFBPMaster> fbpList = payrollFBPMasterRepository
                .existsByFBPMaster(mongoTemplate, range.getRangeId(), jwtHelper.getOrganizationCode());

        Map<String, PayrollFBPMaster> fbpMasterMap = fbpList.stream()
                .collect(Collectors.toMap(PayrollFBPMaster::getFbpType, Function.identity()));

        // Convert existing list to a Map for easy lookup
        Map<String, PayrollFBPPlan.Fbp> existingFbpMap = existingFbpList.stream()
                .collect(Collectors.toMap(PayrollFBPPlan.Fbp::getFbpType, Function.identity()));

        List<Map<String, String>> errorList = new ArrayList<>();

        for (PayrollFBPEmpListDTO fbp : Optional.ofNullable(request.getFbpList()).orElse(Collections.emptyList())) {
            PayrollFBPMaster matchedFbpMaster = fbpMasterMap.get(fbp.getFbpType());
            Map<String, String> error = new HashMap<>();

            if (matchedFbpMaster == null) {
                error.put(fbp.getFbpType(), "Range is invalid");
            } else {
                int requestedAmount = Optional.ofNullable(fbp.getMonthlyAmount()).orElse(0);
                int maxAllowedAmount = matchedFbpMaster.getAmount();

                if (requestedAmount > maxAllowedAmount) {
                    error.put(fbp.getFbpType(), "Amount Exceeds");
                } else {
                    PayrollFBPPlan.Fbp fbpPlan = existingFbpMap.getOrDefault(fbp.getFbpType(), new PayrollFBPPlan.Fbp());
                    fbpPlan.setFbpType(fbp.getFbpType());
                    fbpPlan.setMonthlyAmount(requestedAmount);
                    fbpPlan.setMaxAmount(maxAllowedAmount);
                    fbpPlan.setYearlyAmount(requestedAmount * 12);
                    fbpPlan.setPayrollMonth(payroll.getPayrollMonth());
                    fbpPlan.setFbpStatus("save".equalsIgnoreCase(request.getStatus()) ? 1 : 0);

                    existingFbpMap.put(fbp.getFbpType(), fbpPlan);
                }
            }

            if (!error.isEmpty()) {
                errorList.add(error);
            }
        }

        if (!errorList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, errorList.toString());
        }

        // Update FBP plan details
        empPlan.setFbp(new ArrayList<>(existingFbpMap.values()));
        empPlan.setTotalAmount(empPlan.getFbp().stream().mapToInt(PayrollFBPPlan.Fbp::getMonthlyAmount).sum() * 12);
        empPlan.setStatus("save".equalsIgnoreCase(request.getStatus()) ? DataOperations.SUBMIT.label : DataOperations.DRAFT.label);
        empPlan.setUpdatedBy(empId);
        empPlan.setUpdatedAt(new Date());

        // Save to MongoDB
        boolean update = payrollFBPCreatePlanRepository.updateEmpPlan(
                empId, financialYear, empPlan, mongoTemplate, jwtHelper.getOrganizationCode()
        );

        log.info("Updated Successfully: {}", update);
    }


}
