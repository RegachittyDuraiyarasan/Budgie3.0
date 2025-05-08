package com.hepl.budgie.service.impl.payroll;

import com.beust.ah.A;
import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.AttendanceDateDTO;
import com.hepl.budgie.dto.payroll.AttendanceDateFetchDTO;
import com.hepl.budgie.dto.payroll.PayrollMonthDTO;
import com.hepl.budgie.entity.FinancialYear;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;
import com.hepl.budgie.repository.FinancialYearRepository;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import com.hepl.budgie.service.payroll.PayrollLockMonthService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.DateFormat;
import com.hepl.budgie.utils.PayrollMonthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollLockMonthImpl implements PayrollLockMonthService {

    private final PayrollLockMonthRepository payrollLockMonthRepository;
    private final PayrollMonthProvider payrollMonthProvider;
    private final FinancialYearRepository financialYearRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final Translator translator;

    private Map<String, String> financialYear() {
        return financialYearRepository.findByCountry("India")
                .map(fy -> {
                    Map<String, String> data = new HashMap<>();
                    String[] start = fy.getStartMonthYear().split("-");
                    String[] end = fy.getEndMonthYear().split("-");
                    data.put("finYear", start[0] + "-" + end[0]);
                    data.put("startYear", fy.getStartMonthYear());
                    data.put("endYear", fy.getEndMonthYear());
                    return data;
                })
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, translator.toLocale(AppMessages.FINANCIAL_YEAR_UPDATE)));
    }


    @Override
    public void attendanceDate(AttendanceDateDTO request) {
        log.info("Attendance Info - {}", request);
        Map<String, String> finYear = financialYear();
        Optional<PayrollLockMonth> record = payrollLockMonthRepository.findByFinYearAndOrgId(finYear.get("finYear"), jwtHelper.getOrganizationCode(), mongoTemplate, "IN");
        request.setFinYear(finYear.get("finYear"));
        request.setFromFinYear(finYear.get("startYear"));
        request.setToFinYear(finYear.get("endYear"));
        request.setOrgId(jwtHelper.getOrganizationCode());
        if(record.isPresent()) {
            updateAttendanceDate(finYear.get("finYear"), request, record.get());
            return;
        }
        payrollLockMonthRepository.saveAttendanceDate(request, mongoTemplate, "IN");
    }
    private void updateAttendanceDate(String year, AttendanceDateDTO request, PayrollLockMonth records) {
        for (int i = 0; i < records.getPayrollMonths().size(); i++) {
            PayrollLockMonth.PayrollMonths current = records.getPayrollMonths().get(i);
            if (current.getLockMonth()) {
                current.setStartDate(DateFormat.date(current.getStartDate(), request.getStandardStartDate()));
                current.setEndDate(DateFormat.date(current.getEndDate(), request.getStandardEndDate()));

                if (i + 1 < records.getPayrollMonths().size()) {
                    current = records.getPayrollMonths().get(i + 1);
                    String date = String.valueOf(Integer.parseInt(request.getStandardEndDate()) + 1);
                    current.setStartDate(DateFormat.date(current.getStartDate(), date));
                }
            }
        }
        payrollLockMonthRepository.updateAttendanceDate(year, jwtHelper.getOrganizationCode(), records, mongoTemplate, "IN");
    }
    @Override
    public void generateMonth(String startDate) {
        log.info("Generate Month Service Implementation - " + startDate);
        Optional<FinancialYear> existFinancialYear = financialYearRepository.findByCountry("India");
        if(existFinancialYear.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FINANCIAL_YEAR_UPDATE);
        }

        LocalDate startDateFormatter = LocalDate.parse(existFinancialYear.get().getStartMonthYear() + "-01");
        LocalDate endDateFormatter = YearMonth.from(LocalDate.parse(existFinancialYear.get().getEndMonthYear() + "-01")).atEndOfMonth();

        Optional<PayrollLockMonth> record = payrollLockMonthRepository.findByFinYearAndOrgId(financialYear().get("finYear"), jwtHelper.getOrganizationCode(), mongoTemplate, "IN");
        log.info("Records : {}", record);
        if(record.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ATTENDANCE_DATE_NOT_FOUND);
        }

        String start = record.get().getStandardStartDate();
        String end = record.get().getStandardEndDate();

        PayrollLockMonth payrollLockMonth = new PayrollLockMonth();

        List<PayrollLockMonth.PayrollMonths> payrollMonthsList = new ArrayList<>();

        while (startDateFormatter.isBefore(endDateFormatter) || endDateFormatter.equals(startDateFormatter)) {
            PayrollLockMonth.PayrollMonths payrollMonths = new PayrollLockMonth.PayrollMonths();
            String formattedStartDate = startDateFormatter.format(DateTimeFormatter.ofPattern("yyyy-MM"));

            Date stDate = Date.from(LocalDate.parse(formattedStartDate + "-" + start).minusMonths(1).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());
            Date enDate = Date.from(LocalDate.parse(formattedStartDate + "-" + end).atStartOfDay().atZone(ZoneOffset.UTC).toInstant());

            payrollMonths.setStartDate(stDate);
            payrollMonths.setEndDate(enDate);
            payrollMonths.setPayrollMonth(startDateFormatter.format(DateTimeFormatter.ofPattern("MM-yyyy")));
            payrollMonths.setLockMonth(startDateFormatter.equals(LocalDate.parse(startDate + "-01")));

            payrollMonthsList.add(payrollMonths);

            startDateFormatter = startDateFormatter.plusMonths(1);
        }
        payrollLockMonth.setPayrollMonths(payrollMonthsList);

        payrollLockMonthRepository.savePayrollMonth(financialYear().get("finYear"), jwtHelper.getOrganizationCode(), payrollLockMonth, mongoTemplate, "IN");
    }
    @Override
    public void updateLockMonth(String payrollMonth) {
        log.info("Payroll Month Info - {}", payrollMonth);
        Optional<PayrollLockMonth> optionalRecord = payrollLockMonthRepository.findByFinYearAndOrgId(financialYear().get("finYear"), jwtHelper.getOrganizationCode(), mongoTemplate, "IN");
        PayrollLockMonth payrollLockMonth = optionalRecord.orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.FINANCIAL_YEAR_UPDATE));

        if(payrollLockMonth.getPayrollMonths() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.GENERATE_MONTH);
        }

        List<PayrollLockMonth.PayrollMonths> payrollMonths = payrollLockMonth.getPayrollMonths();

        boolean monthFound = false;
        for (int i = 0; i < payrollMonths.size(); i++) {
            PayrollLockMonth.PayrollMonths record = payrollMonths.get(i);
            boolean isLast = (i == payrollMonths.size() - 1);

            if (record.getPayrollMonth().equals(payrollMonth)) {
                monthFound = true;

                if (record.getLockMonth() && isLast) {
                    String nextMonth = LocalDate.parse("01-" + payrollMonth, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                            .plusMonths(1)
                            .format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    this.generateMonth(nextMonth);
                    return;
                } else {
                    record.setLockMonth(true);
                }
            } else {
                record.setLockMonth(false);
            }
        }
        if (!monthFound) {
            throw new CustomResponseStatusException(AppMessages.PAYROLL_MONTH_NOT_FOUND, HttpStatus.BAD_REQUEST, new Object[]{payrollMonth});
        }
        payrollLockMonthRepository.savePayrollMonth(financialYear().get("finYear"), jwtHelper.getOrganizationCode(), payrollLockMonth, mongoTemplate, "IN");
        payrollMonthProvider.refreshPayrollMonth();
    }

    @Override
    public AttendanceDateFetchDTO standardDate() {
        PayrollLockMonth payroll =  payrollLockMonthRepository.getAttendanceDate(financialYear().get("finYear"), mongoTemplate, jwtHelper.getOrganizationCode(), "IN").orElseThrow(()-> new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ATTENDANCE_DATE_NOT_FOUND));
        return AttendanceDateFetchDTO.builder().standardStartDate(payroll.getStandardStartDate()).standardEndDate(payroll.getStandardEndDate()).build();
    }

    @Override
    public List<PayrollMonthDTO> listPayrollMonth() {
        return payrollLockMonthRepository.getPayrollMonths(financialYear().get("finYear"), mongoTemplate, jwtHelper.getOrganizationCode(), "IN");
    }
}
