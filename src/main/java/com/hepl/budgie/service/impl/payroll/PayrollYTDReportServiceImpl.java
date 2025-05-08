package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.entity.payroll.PayrollCTCBreakups;
import com.hepl.budgie.entity.payroll.PayrollPaySheet;
import com.hepl.budgie.entity.payroll.PayrollYTDRecord;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.repository.payroll.*;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.payroll.PayrollYTDReportService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.PayrollDateFormat;
import com.hepl.budgie.utils.PayrollMonthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class PayrollYTDReportServiceImpl implements PayrollYTDReportService {

    private final JWTHelper jwtHelper;
    private final PayrollLockMonthRepository lockMonthRepository;
    private final PayrollComponentRepository componentRepository;
    private final PayrollCTCBreakupsRepository ctcBreakupsRepository;
    private final PayrollPaySheetRepository paySheetRepository;
    private final UserInfoRepository userInfoRepository;
    private final MongoTemplate mongoTemplate;
    private final PayrollMonthProvider payrollMonthProvider;
    private final PayrollYTDRecordRepository ytdRecordRepository;

    public List<String> list() {
        String empId = "TEMP011";
//        List<String> monthList = lockMonthRepository.getActivePayrollMonths(mongoTemplate, getOrgCode(), "IN");

//        List<PayrollGroupedComponentDTO> components = componentRepository.componentType(mongoTemplate, getOrgCode());
//        log.info("month List -{}", components);

        List<String> payrollMonthsForEmployee = eligiblePayrollMonthsForEmployee(empId);
        log.info("month List -{}", payrollMonthsForEmployee);

        List<PayrollPaySheet> paySheetList = paySheetRepository.findByEmpIdAndPayrollMonthIn(mongoTemplate, getOrgCode(), payrollMonthsForEmployee, empId);

        int ctcProjectionCount = payrollMonthsForEmployee.size() - paySheetList.size();
        log.info("count -{}", ctcProjectionCount);
        Optional<PayrollCTCBreakups> ctcBreakups = ctcBreakupsRepository.findByEmpIdAndRevisionOrderDesc(empId, mongoTemplate, getOrgCode());

        List<Integer> basicTen = new ArrayList<>();
        List<Integer> basicFiftyForty = new ArrayList<>();
        List<Integer> hra = new ArrayList<>();
        PayrollMonth payrollMonth = payrollMonthProvider.getPayrollMonth();
        // Merge earnings and variables in pay sheet
        Map<String, Integer> earnings = paySheetList.stream()
                .flatMap(p -> Stream.of(p.getEarnings(), p.getVariables()))
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));

        // Merge deductions
        Map<String, Integer> deductions = paySheetList.stream()
                .map(PayrollPaySheet::getDeductions)
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));

        // Merge reimbursements
        Map<String, Integer> reimbursement = paySheetList.stream()
                .map(PayrollPaySheet::getReimbursements)
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));
        //Earnings Arrears
        Map<String, Integer> earningArrears = paySheetList.stream()
                .map(PayrollPaySheet::getArrears)
                .filter(Objects::nonNull)
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        Integer::sum
                ));
        log.info("Earnings: {}", earnings);

        // Collect basic & HRA from pay sheet
        paySheetList.stream()
                .map(PayrollPaySheet::getEarnings)
                .filter(Objects::nonNull)
                .forEach(earnMap -> {
                    earnMap.forEach((key, value) -> {
                        if (key.equalsIgnoreCase("basic")) {
                            basicTen.add((int) Math.round(value * 0.10));
                            basicFiftyForty.add((int) Math.round(value * 0.40));
                        } else if (key.equalsIgnoreCase("hra")) {
                            hra.add(value);
                        }
                    });
                });

        // CTC processing
        Map<String, Integer> paySheetAndCtcEarnings = new HashMap<>(earnings);

        ctcBreakups.ifPresent(ctc -> {
            Map<String, Integer> ctcEarnings = new HashMap<>();

            ctc.getEarningColumns().forEach((key, value) -> {
                double monthly = value / 12.0;
                int projected = (int) Math.round(monthly * ctcProjectionCount);
                ctcEarnings.put(key, projected);

                if (key.equalsIgnoreCase("basic")) {
                    basicTen.add(value);
                    IntStream.rangeClosed(1, ctcProjectionCount).forEach(i -> {
                        basicTen.add((int) Math.round(monthly * 0.10));
                        basicFiftyForty.add((int) Math.round(monthly * 0.40));
                    });
                } else if (key.equalsIgnoreCase("hra")) {
                    IntStream.rangeClosed(1, ctcProjectionCount).forEach(i ->
                            hra.add((int) Math.round(monthly))
                    );
                }
            });

            log.info("CTC Earnings: {}", ctcEarnings);

            // Merge earnings with CTC earnings
            ctcEarnings.forEach((key, val) ->
                    paySheetAndCtcEarnings.merge(key, val, Integer::sum));
        });

        log.info("Final Combined Earnings: {}", paySheetAndCtcEarnings);

        int annualGrossIncome = earningArrears.values().stream().mapToInt(Integer::intValue).sum() +
                paySheetAndCtcEarnings.values().stream().mapToInt(Integer::intValue).sum();

        PayrollYTDRecord payrollYTDRecord = new PayrollYTDRecord();
        payrollYTDRecord.setEmpId(empId);
        payrollYTDRecord.setPayrollMonth(payrollMonth.getPayrollMonth());
        payrollYTDRecord.setFinancialYear(payrollMonth.getFinYear());
        payrollYTDRecord.setDeductions(deductions);
        payrollYTDRecord.setEarnings(earnings);
        payrollYTDRecord.setReimbursements(reimbursement);
        payrollYTDRecord.setArrears(earningArrears);
        payrollYTDRecord.setHra(hra);
        payrollYTDRecord.setBasicTen(basicTen);
        payrollYTDRecord.setBasicFiftyForty(basicFiftyForty);
        payrollYTDRecord.setAnnualGrossIncome(annualGrossIncome);
        log.info("YTD Records-{}", payrollYTDRecord);
        ytdRecordRepository.insert(mongoTemplate, getOrgCode(), payrollYTDRecord);
        return List.of();

    }

    private List<String> eligiblePayrollMonthsForEmployee(String empId) {

        //Get Employee DOJ and DOL
        WorkingInformation workingInformation = userInfoRepository.getWorkInfo(mongoTemplate, empId);
        LocalDate doj = workingInformation.getDoj().toLocalDate();
        LocalDate dol = workingInformation.getDateOfRelieving() != null ? workingInformation.getDateOfRelieving().toLocalDate() : null;
        log.info("working Information -{}, dol -{}", doj, dol);

        //Get Financial Year Start Date and End Date
        PayrollMonth payrollMonth = payrollMonthProvider.getPayrollMonth();
        YearMonth finYearStartMonth = YearMonth.parse(payrollMonth.getFromFinYear(), DateTimeFormatter.ofPattern(PayrollDateFormat.YEAR_MONTH_FORMAT));
        YearMonth finYearToMonth = YearMonth.parse(payrollMonth.getToFinYear(), DateTimeFormatter.ofPattern(PayrollDateFormat.YEAR_MONTH_FORMAT));
        LocalDate finYearStartDate = finYearStartMonth.atDay(1);
        LocalDate finYearEndDate = finYearToMonth.atEndOfMonth();

        LocalDate startDate = doj.isAfter(finYearStartDate) ? doj : finYearStartDate;
        LocalDate endDate = dol != null && dol.isBefore(finYearEndDate) ? dol : finYearEndDate;

        return Stream.iterate(YearMonth.from(startDate), month -> month.plusMonths(1))
                .limit(startDate.until(endDate, ChronoUnit.MONTHS) + 1)
                .map(yearMonth -> yearMonth.format(DateTimeFormatter.ofPattern(PayrollDateFormat.MONTH_YEAR_FORMAT)))
                .collect(Collectors.toList());

    }

    private String getOrgCode() {
        return jwtHelper.getOrganizationCode();
    }

}
