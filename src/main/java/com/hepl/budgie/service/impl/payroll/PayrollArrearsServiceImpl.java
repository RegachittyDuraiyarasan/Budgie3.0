package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.dto.payroll.PayrollArrearsCompDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsListDTO;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.payroll.PayrollArrears;
import com.hepl.budgie.entity.payroll.PayrollCTCBreakups;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import com.hepl.budgie.repository.attendancemanagement.AttendanceMusterRepository;
import com.hepl.budgie.repository.payroll.PayrollArrearsRepository;
import com.hepl.budgie.repository.payroll.PayrollCTCBreakupsRepository;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.service.payroll.PayrollArrearsService;
import com.hepl.budgie.utils.*;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Service
@Slf4j
public class PayrollArrearsServiceImpl implements PayrollArrearsService {

    private final PayrollMonthProvider payrollMonthProvider;
    private final PayrollCTCBreakupsRepository ctcBreakupsRepository;
    private final PayrollComponentRepository componentRepository;
    private final PayrollArrearsRepository arrearsRepository;
    private final MongoTemplate mongoTemplate;
    private final AttendanceMusterRepository attendanceMusterRepository;
    private final PayrollLockMonthRepository lockMonthRepository;
    private final JWTHelper jwtHelper;
    private final Translator translator;

    public List<PayrollCTCBreakupsDTO> newJoinerArrears() {

        String payrollMonth = payrollMonthProvider.getPayrollMonth().getPayrollMonth();
        ZonedDateTime startDate = payrollMonthProvider.getPayrollMonth().getStartDate();

        YearMonth yearMonth = YearMonth.from(startDate);
        LocalDate lastDay = yearMonth.atEndOfMonth();
        int daysInMonth = yearMonth.lengthOfMonth();

        // Formatted end date
        ZonedDateTime endDate = lastDay.atStartOfDay(startDate.getZone());
        log.info("Start Date -{}", startDate);
        log.info("End Date -{}", endDate);

        List<PayrollCTCBreakupsDTO> ctcBreakupsDTOList = ctcBreakupsRepository
                .getNewJoinerCTC(mongoTemplate, getOrgCode(), startDate, endDate);
        log.info("Employees with CTC Breakups -{}", ctcBreakupsDTOList);

        if (ctcBreakupsDTOList.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.EMP_NOT_FOUND);
        }

        List<Map<String, Object>> empWithNoCTC = new ArrayList<>();
        List<PayrollArrears> payrollArrearsList = new ArrayList<>();


        List<PayrollArrearsCompDTO> components = componentRepository.getArrearsComponents(
                mongoTemplate,
                PayrollArrearsCompDTO.class,
                getOrgCode()
        );

        ctcBreakupsDTOList.forEach(data -> {
            if (data.getRevisionOrder() == 0) {
                empWithNoCTC.add(
                        Map.of("empId", data.getEmpId(),
                                "message", translator.toLocale(AppMessages.CTC_NOT_FOUND)
                        )
                );
            } else {

                long workDays = ChronoUnit.DAYS.between(data.getDoj().toLocalDate(), endDate.toLocalDate()) + 1;
                log.info("workDays {}", workDays);

                Map<String, Integer> arrears = new LinkedHashMap<>();
                components.forEach(comp -> {
                    int monthlyValue = data.getEarningColumns().getOrDefault(comp.getComponentSlug(), 0) / 12;
                    int arrValue = Math.round(((float) monthlyValue / daysInMonth) * workDays);
                    arrears.put(comp.getComponentSlug(), arrValue);
                });

                PayrollArrears payrollArrears = new PayrollArrears();
                payrollArrears.setEmpId(data.getEmpId());
                payrollArrears.setWithEffectDate(data.getWithEffectDate());
                payrollArrears.setPayrollMonth(payrollMonth);
                payrollArrears.setArrearsValues(arrears);
                payrollArrears.setArrDays(workDays);
                payrollArrears.setGross(arrears.values().stream().mapToInt(Integer::intValue).sum());
                payrollArrears.setArrType(VariablesType.NEW_JOINER.getLabel());
                payrollArrearsList.add(payrollArrears);

            }
        });

        BulkWriteResult result = arrearsRepository.bulkUpsert(mongoTemplate, getOrgCode(), payrollArrearsList);
        log.info("EMP With no CTC -{}", empWithNoCTC);

        return ctcBreakupsDTOList;
    }

    public List<List<String>> processExistingEmpArrears(List<String> employeeId, LocalDate withEffectDate) {

        List<String[]> data = new ArrayList<>();

        String payrollMonth = payrollMonthProvider.getPayrollMonth().getPayrollMonth();
        LocalDate payrollMonthFormatted = LocalDate.parse("01-" + payrollMonth, DateTimeFormatter.ofPattern(PayrollDateFormat.DATE_FORMAT));
        LocalDate previousMonthStartDate = payrollMonthFormatted.minusMonths(1);
        log.info("previousMonthStartDate -{}", previousMonthStartDate);

        List<PayrollCTCBreakupsListDTO> ctcBreakupsDTOList = ctcBreakupsRepository
                .findByEmpIdIn(mongoTemplate, getOrgCode(), employeeId);
        log.info("Employees with CTC Breakups -{}", ctcBreakupsDTOList);

        List<PayrollArrearsCompDTO> components = componentRepository.getArrearsComponents(
                mongoTemplate,
                PayrollArrearsCompDTO.class,
                getOrgCode()
        );

        List<PayrollArrears> payrollArrearsList = new ArrayList<>();

        ctcBreakupsDTOList.forEach(ctc -> {
            if (ctc.getDoj() == null) {
                data.add(new String[]{ctc.getEmpId(), "Date of Joining is not added"});
                return;
            }

            List<PayrollCTCBreakups> ctcBreakups = ctc.getCtcBreakups();
            if (ctcBreakups.isEmpty() || ctcBreakups.size() == 1) {
                data.add(new String[]{ctc.getEmpId(), ctcBreakups.isEmpty()  ? "CTC not found for this employee" : "CTC not revised for this employee"});
                return;
            }

//            log.info("CTC - {}", ctcBreakups);
            List<PayrollCTCBreakups> lastTwoCtc = ctcBreakups
                    .stream()
                    .sorted(Comparator.comparingInt(PayrollCTCBreakups::getRevisionOrder).reversed())
                    .limit(2)
                    .toList();

            PayrollCTCBreakups currentCtc = lastTwoCtc.get(0);
            PayrollCTCBreakups oldCtc = lastTwoCtc.get(1);

            LocalDate effectiveDate = withEffectDate.isBefore(ctc.getDoj().toLocalDate())
                    ? ctc.getDoj().toLocalDate()
                    : withEffectDate;

            Map<String, Integer> arrearsList = new LinkedHashMap<>();
            List<String> payrollMonthList = DateUtil.createDatePeriods(effectiveDate, DateUtil.ONE_MONTH_PERIOD, payrollMonthFormatted);
            List<LopDTO> lopData = attendanceMusterRepository.findByEmpIdAndMonth(mongoTemplate, getOrgCode(), ctc.getEmpId(), payrollMonthList);
//            log.info("lopData - {}", lopData);
            double totalWorkDaysSum = 0;

            for (String monthList : payrollMonthList) {
                LocalDate startMonth = LocalDate.parse("01-" + monthList, DateTimeFormatter.ofPattern(PayrollDateFormat.DATE_FORMAT));
                YearMonth yearMonth = YearMonth.from(startMonth);
                int daysInMonth = yearMonth.lengthOfMonth();

                LocalDate endMonth = yearMonth.atEndOfMonth();
                int arrWorkDays = daysInMonth;

                if (effectiveDate.isAfter(startMonth) && effectiveDate.isBefore(endMonth)) {
                    startMonth = effectiveDate;
                    arrWorkDays = (int) ChronoUnit.DAYS.between(effectiveDate, endMonth) + 1;
                }

                LopDTO lopDTO = lopData.stream().filter(lop -> lop.getMonthYear().equalsIgnoreCase(monthList)).findFirst().orElse(null);
                double lop = lopDTO != null ? lopDTO.getLop() - lopDTO.getLopReversal() : 0.0;
                double workDays = arrWorkDays - lop;
                totalWorkDaysSum += workDays;

                for(PayrollArrearsCompDTO comp : components) {
                    int yearlyDiff = currentCtc.getEarningColumns().getOrDefault(comp.getComponentSlug(), 0)
                            - oldCtc.getEarningColumns().getOrDefault(comp.getComponentSlug(), 0);
                    int arrValue = (int) Math.round((yearlyDiff / 12.0 / daysInMonth) * workDays);
                    arrearsList.merge(comp.getComponentSlug(), arrValue, Integer::sum);
//                    log.info("Component Value-{}, Yearly Value -{}, Diff Value -{}, arrears value -{}", comp.getComponentSlug() ,YearlyDiffValue, monthlyDiffValue, arrValue);
//                    log.info("Payroll Month -{} , daysInMonth -{}, workDays -{}", monthList, daysInMonth , workDays);
                }

            }
            PayrollArrears payrollArrears = new PayrollArrears();
            payrollArrears.setPayrollMonth(payrollMonth);
            payrollArrears.setEmpId(ctc.getEmpId());
            payrollArrears.setArrearsValues(arrearsList);
            payrollArrears.setWithEffectDate(AppUtils.parseLocalDate(effectiveDate, LocaleContextHolder.getTimeZone().getID()));
            payrollArrears.setArrType("Revised CTC Arrears");
            payrollArrears.setGross(arrearsList.values().stream().mapToInt(Integer::intValue).sum());
            payrollArrears.setArrDays(totalWorkDaysSum);

            payrollArrearsList.add(payrollArrears);
//                log.info("Arrears List-{}", arrearsList);

        });
        if(!payrollArrearsList.isEmpty()) {
            BulkWriteResult result = arrearsRepository.bulkUpsert(mongoTemplate, getOrgCode(), payrollArrearsList);

            result.getInserts().forEach(writeInsert -> {
                data.add(new String[]{payrollArrearsList.get(writeInsert.getIndex()).getEmpId(), "Inserted Successfully"});
            });
        }

        return data.stream().map(Arrays::asList).toList();
    }

    public List<Map<String, Object>> arrearsList(String month) {

        String payrollMonth = month.isEmpty() ? payrollMonthProvider.getPayrollMonth().getPayrollMonth() : month;
        List<PayrollArrearsCompDTO> components = componentRepository.getArrearsComponents(mongoTemplate, PayrollArrearsCompDTO.class, getOrgCode());

        return arrearsRepository.getByPayrollMonth(mongoTemplate, getOrgCode(), payrollMonth)
                .stream()
                .map(dt -> {
                    Map<String, Object> objectMap = new LinkedHashMap<>();

                    objectMap.put("empId", dt.getEmpId());
                    objectMap.put("empName", dt.getEmpName());
                    components.forEach(comp -> {
                        objectMap.put(comp.getComponentSlug(), dt.getArrearsValues().getOrDefault(comp.getComponentSlug(), 0));
                    });
                    objectMap.put("gross", dt.getGross());

                    return objectMap;

                }).toList();


    }

    public List<String> headers() {
        return componentRepository.getArrearsComponents(
                        mongoTemplate,
                        PayrollPayTypeCompDTO.class,
                        getOrgCode()
                )
                .stream()
                .map(PayrollPayTypeCompDTO::getComponentName)
                .toList();
    }

    private String getOrgCode() {
        return "ORG00001";
//        return jwtHelper.getOrganizationCode();
    }
}
