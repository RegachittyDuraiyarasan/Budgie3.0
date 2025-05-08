package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PTListDTO;
import com.hepl.budgie.dto.payroll.PayrollPtDTO;
import com.hepl.budgie.entity.payroll.PayrollPt;
import com.hepl.budgie.repository.payroll.PayrollPTRepository;
import com.hepl.budgie.service.payroll.PayrollPtService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Stream;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollPtImpl implements PayrollPtService {

    private final PayrollPTRepository payrollPTRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<PTListDTO> list() {
        log.info("Payroll PT Impl List");
        List<PayrollPt> getPt = payrollPTRepository.getAllPTRecords(jwtHelper.getOrganizationCode(), mongoTemplate);
        log.info("List : {}", getPt);
        List<PTListDTO> result = getPt.stream()
                .filter(payrollPt -> payrollPt.getDeductionMonDetails() != null) // Filter out null DeductionMonDetails
                .flatMap(payrollPt -> {
                    Object deductionDetails = payrollPt.getDeductionMonDetails();
                    List<PayrollPt.RangeDetails> rangeDetailsList = payrollPt.getRangeDetails();
                    if (deductionDetails instanceof String) {
                        // Handle String type deduction details
                        String deductionMonth = String.valueOf(deductionDetails);
                        return rangeDetailsList.stream()
                                .map(rangeDetails -> {
                                    PTListDTO resultEntry = new PTListDTO();
                                    resultEntry.setPtId(payrollPt.getPtId());
                                    resultEntry.setState(payrollPt.getState());
                                    resultEntry.setPeriodicity(payrollPt.getPeriodicity());
                                    resultEntry.setDeductionType(payrollPt.getDeductionType());
                                    resultEntry.setStartToEnd("-");
                                    resultEntry.setDeductionMonth(deductionMonth);
                                    resultEntry.setSalaryFrom(String.valueOf(rangeDetails.getSalaryFrom()));
                                    resultEntry.setSalaryTo(String.valueOf(rangeDetails.getSalaryTo()));
                                    resultEntry.setTaxAmount(String.valueOf(rangeDetails.getTaxAmount()));
                                    resultEntry.setGender(rangeDetails.getGender());
                                    return resultEntry;
                                });
                    } else if (deductionDetails instanceof List) {
                        // Handle List type deduction details
                        @SuppressWarnings("unchecked")
                        List<Map<String, String>> deductionDetailList = (List<Map<String, String>>) deductionDetails;
                        List<String> pair = List.of("1st", "2nd");

                        return deductionDetailList.stream()
                                .flatMap(detail -> rangeDetailsList.stream().map(rangeDetails -> {
                                    int index = deductionDetailList.indexOf(detail); // Find the index dynamically
                                    String startDate = detail.getOrDefault(pair.get(index) + "HYStartDate", "");
                                    String endDate = detail.getOrDefault(pair.get(index) + "HYEndDate", "");
                                    String deductionMonth = detail.getOrDefault(pair.get(index) + "HYDeductionMon", "");

                                    PTListDTO resultEntry = new PTListDTO();
                                    resultEntry.setPtId(payrollPt.getPtId());
                                    resultEntry.setState(payrollPt.getState());
                                    resultEntry.setPeriodicity(payrollPt.getPeriodicity());
                                    resultEntry.setDeductionType(payrollPt.getDeductionType());
                                    resultEntry.setStartToEnd(startDate + " - " + endDate);
                                    resultEntry.setDeductionMonth(deductionMonth);
                                    resultEntry.setSalaryFrom(String.valueOf(rangeDetails.getSalaryFrom()));
                                    resultEntry.setSalaryTo(String.valueOf(rangeDetails.getSalaryTo()));
                                    resultEntry.setTaxAmount(String.valueOf(rangeDetails.getTaxAmount()));
                                    resultEntry.setGender(rangeDetails.getGender());
                                    return resultEntry;
                                }));
                    }
                    return Stream.empty();
                })
                .toList();
        return result;
    }

    @Override
    public void add(PayrollPtDTO request) {
        log.info("PT Add Info : {}",request.getRangeDetails());
        Optional<PayrollPt> exist = payrollPTRepository.existByState(jwtHelper.getOrganizationCode(), request, mongoTemplate);
        if (exist.isPresent()) {

            // State Already Exist
            if(exist.get().getState().equalsIgnoreCase(request.getState()) && !exist.get().getPeriodicity().equalsIgnoreCase(request.getPeriodicity())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.ERROR_STATE_EXIST); // Change the error status.
            }

            // Get PT Details from the database
            PayrollPt getPt = exist.get();
            List<PayrollPt.RangeDetails> getRangeDetails = getPt.getRangeDetails();


            processDeductionMonDetails(request);


            request.getRangeDetails().forEach(rangeDetails -> {
                boolean matchedRangeDetails = payrollPTRepository.checkRangeDetails(jwtHelper.getOrganizationCode(), rangeDetails, mongoTemplate);
                if (matchedRangeDetails) {
                    // Construct the detailed error message
                    Map<String, String> errorDetails = new HashMap<>();
                    errorDetails.put("salaryFrom", "Value is matched in the database value range");
                    errorDetails.put("salaryTo", "Value is matched in the database value range");
                    // Throw the exception with the details
                    throw new CustomResponseStatusException(AppMessages.UNSUPPORTED_FORMAT,HttpStatus.BAD_REQUEST , new Object[] {errorDetails});
                }
            });

//            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.ERROR_STATE_EXIST);
        }
        request.setPtId(payrollPTRepository
                .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                .map(e-> AppUtils.generateUniqueId(e.getPtId()))
                .orElse("PT00001"));
        payrollPTRepository.addPT(jwtHelper.getOrganizationCode(), request, mongoTemplate);
    }

    private void processDeductionMonDetails(PayrollPtDTO request) {
        log.info("Entered into an deduction month details");

        Object deductionMonDetails = request.getDeductionMonDetails();

        // Check if `deductionMonDetails` is a string
        if (deductionMonDetails instanceof String) {
            System.out.println("Deduction Month Details: " + deductionMonDetails);
        }
        // Check if `deductionMonDetails` is an object (assumed to be a list of maps)
        else if (deductionMonDetails instanceof List) {
            List<?> deductionDetailsList = (List<?>) deductionMonDetails;

            for (Object detail : deductionDetailsList) {
                if (detail instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, String> detailMap = (Map<String, String>) detail;
                    System.out.println("Processed Deduction Detail: " + detailMap);
                }
            }
        } else {
            System.out.println("Unknown type for deductionMonDetails.");
        }
    }
}
