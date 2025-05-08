package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.entity.payroll.PayrollComponent;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.repository.payroll.PayrollCTCBreakupsRepository;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.service.payroll.CTCBreakupsService;
import com.hepl.budgie.utils.PayrollMonthProvider;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;


import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class CTCBreakupsServiceImpl implements CTCBreakupsService {

    private final PayrollComponentRepository componentRepository;
    private final MongoTemplate mongoTemplate;
    private final PayrollCTCBreakupsRepository ctcBreakupsRepository;
    private final JWTHelper jwtHelper;
    private final PayrollMonthProvider payrollMonthProvider;


    public BulkWriteResult excelImport(List<Map<String, Object>> validRows)  {

        List<PayrollComponent> payrollComponents = componentRepository.getFixedComponents(mongoTemplate, getOrgCode(), List.of(ComponentType.EARNINGS.label, ComponentType.DEDUCTION.label));

        List<PayrollComponent> earnings = payrollComponents.stream()
                .filter(val -> ComponentType.EARNINGS.label.equalsIgnoreCase(val.getComponentType()))
                .toList();

        List<PayrollComponent> deductions = payrollComponents.stream()
                .filter(val -> ComponentType.DEDUCTION.label.equalsIgnoreCase(val.getComponentType()))
                .toList();

        List<PayrollCTCBreakupsDTO> payrollCTCBreakupsDTOS = new ArrayList<>();

        log.info("Valid Row -{}", validRows);

        for (Map<String, Object> list : validRows) {
            Map<String, Integer> earningsData = new HashMap<>();
            earnings.forEach(component -> {
                String headerName = component.getComponentName().replaceAll("\\s+", "_") + "_(Annual)";
                int value = (int) list.getOrDefault(headerName, 0);
                earningsData.put(component.getComponentSlug(), value);
            });
            Map<String, Integer> deductionData = new HashMap<>();
            deductions.forEach(component -> {
                String headerName = component.getComponentName().replaceAll("\\s+", "_") + "_(Annual)";
                int value = (int) list.getOrDefault(headerName, 0);
                deductionData.put(component.getComponentSlug(), value);
            });
            int grossEarnings = earningsData.values().stream().mapToInt(Integer::intValue).sum();
            int grossDeduction = deductionData.values().stream().mapToInt(Integer::intValue).sum();
            int netPay = grossEarnings - grossDeduction;
            String empId = list.get("Employee_ID").toString();
            ZonedDateTime withEffectDate = ((LocalDate) list.get("With_Effect_Date")).atStartOfDay(ZoneId.systemDefault());
            int revisionOrder = ctcBreakupsRepository
                    .findLatestEmpCtc(mongoTemplate, getOrgCode(), empId)
                    .map(e -> e.getRevisionOrder() + 1)
                    .orElse(1);
            PayrollCTCBreakupsDTO ctcBreakupsDTO = new PayrollCTCBreakupsDTO();
            ctcBreakupsDTO.setEmpId(empId);
            ctcBreakupsDTO.setFinancialYear(payrollMonthProvider.getPayrollMonth().getFinYear());
            ctcBreakupsDTO.setPayrollMonth(payrollMonthProvider.getPayrollMonth().getPayrollMonth());
            ctcBreakupsDTO.setWithEffectDate(withEffectDate);
            ctcBreakupsDTO.setRevisionOrder(revisionOrder);
            ctcBreakupsDTO.setEarningColumns(earningsData);
            ctcBreakupsDTO.setDeductionColumn(deductionData);
            ctcBreakupsDTO.setGrossDeductions(grossDeduction);
            ctcBreakupsDTO.setGrossEarnings(grossEarnings);
            ctcBreakupsDTO.setNetPay(netPay);
            payrollCTCBreakupsDTOS.add(ctcBreakupsDTO);

        }
        log.info("Payroll CTC Breakups DTO {}", payrollCTCBreakupsDTOS);
        return ctcBreakupsRepository.bulkUpsert(payrollCTCBreakupsDTOS, mongoTemplate, getOrgCode());


    }

    public List<Map<String, Object>> list() {
        return buildMonthlyCTCBreakup(ctcBreakupsRepository.findAllByOrgIdWithUserInfo(mongoTemplate, getOrgCode()));
    }
    public List<Map<String, Object>> singleEmpCTC(String empId) {
        return buildMonthlyCTCBreakup(ctcBreakupsRepository.findByEmpId(mongoTemplate, getOrgCode(), empId));
    }

    public List<String> dataTableHeaders() {
        List<String> earnings = fixedComponents().get(ComponentType.EARNINGS.label.toLowerCase());
        List<String> headers = new ArrayList<>(earnings);
        headers.add("Gross Earnings");
        headers.addAll(fixedComponents().get(ComponentType.DEDUCTION.label.toLowerCase()));
        headers.add("Gross Deductions");
        headers.add("Net Pay");
        headers.add("Revision Order");
        return headers;
    }

    private Map<String, List<String>> fixedComponents() {
        List<PayrollComponent> payrollComponents = componentRepository.getFixedComponents(mongoTemplate, getOrgCode(), List.of(ComponentType.EARNINGS.label, ComponentType.DEDUCTION.label));

        List<String> earnings = payrollComponents.stream()
                .filter(val -> ComponentType.EARNINGS.label.equalsIgnoreCase(val.getComponentType()))
                .map(PayrollComponent::getComponentName)
                .toList();
        log.info("Payroll  Earnings Component {}", earnings);
        List<String> deductions = payrollComponents.stream()
                .filter(val -> ComponentType.DEDUCTION.label.equalsIgnoreCase(val.getComponentType()))
                .map(PayrollComponent::getComponentName)
                .toList();
        log.info("Payroll Deduction Component {}", deductions);
        return Map.of(ComponentType.EARNINGS.label.toLowerCase(), earnings, ComponentType.DEDUCTION.label.toLowerCase(), deductions);
    }

    private List<Map<String, Object>> buildMonthlyCTCBreakup(List<PayrollCTCBreakupsDTO> dto) {
        List<String> earnings = fixedComponents().get(ComponentType.EARNINGS.label.toLowerCase());
        log.info("Payroll  Earnings Component {}", earnings);
        List<String> deductions = fixedComponents().get(ComponentType.DEDUCTION.label.toLowerCase());
        log.info("Payroll Deduction Component {}", deductions);
        return dto.stream()
                .map(list -> {
                    Map<String, Object> ctc = new LinkedHashMap<>();
                    ctc.put("empId", list.getEmpId());
                    ctc.put("name", list.getEmpName());
                    ctc.put("withEffectDate", list.getWithEffectDate());
                    earnings.stream()
                            .map(data -> data.toLowerCase().replaceAll("\\s+", "_"))
                            .forEach(earning -> {
                                        int value = list.getEarningColumns().getOrDefault(earning, 0);
                                        ctc.put(earning, value / 12);
                                    }
                            );
                    deductions.stream()
                            .map(data -> data.toLowerCase().replaceAll("\\s+", "_"))
                            .forEach(deduction -> {
                                        int value = list.getDeductionColumn().getOrDefault(deduction.toLowerCase().replaceAll("\\s+", "_"), 0);
                                        ctc.put(deduction, value / 12);
                                    }
                            );
                    ctc.put("gross_earnings", list.getGrossEarnings() / 12);
                    ctc.put("gross_deductions", list.getGrossDeductions() / 12);
                    ctc.put("net_pay", list.getNetPay() / 12);
                    ctc.put("revision_order", list.getRevisionOrder());
                    return ctc;

                }).toList();

    }

    public void initCTCIndexingForOrg(String organisation) {
        ctcBreakupsRepository.initCTCIndexing(organisation, mongoTemplate);
    }

    private String getOrgCode() {
//        return "ORG00001";
        return jwtHelper.getOrganizationCode();
    }
}
