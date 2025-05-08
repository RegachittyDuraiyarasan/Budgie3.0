package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.attendancemanagement.LopDTO;
import com.hepl.budgie.dto.payroll.PaySheetMonthInfoDTO;
import com.hepl.budgie.dto.payroll.PayrollCTCBreakupsDTO;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.dto.payroll.PayrollPaysheetDTO;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.Sequence;
import com.hepl.budgie.entity.payroll.PayrollCTCBreakups;
import com.hepl.budgie.entity.payroll.payrollEnum.DataOperations;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.repository.payroll.PayrollCTCBreakupsRepository;
import com.hepl.budgie.repository.payroll.PayrollPaySheetRepository;
import com.hepl.budgie.service.payroll.PayrollPaySheetService;
import com.hepl.budgie.utils.*;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class PayrollPaySheetServiceImpl implements PayrollPaySheetService {

    private final PayrollPaySheetRepository payrollPaysheetRepository;
    private final PayrollCTCBreakupsRepository payrollCTCBreakupsRepository;
    private final PayrollMonthProvider payrollMonthProvider;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final Translator translator;
    private final OrganizationRepository organizationRepository;

    public Map<String, Object> getPayrollStatus(){
        Optional<Organization> organization = organizationRepository.findByOrganizationCode(jwtHelper.getOrganizationCode());
        boolean processingPayroll = payrollPaysheetRepository.existsByStatus(mongoTemplate, jwtHelper.getOrganizationCode(), DataOperations.PROCESSING.label);
        String msg = processingPayroll ? "Please wait paysheet is already running, try again after sometimes." : "-";
        return Map.of("status", !processingPayroll, "payrollStatus", getStatus(), "message" , msg );

    }
    private List<String> getStatus(){
        Optional<Organization> organization = organizationRepository.findByOrganizationCode(jwtHelper.getOrganizationCode());
        List<String> payrollStatus = new ArrayList<>();
        if (organization.isPresent()) {
            List<Sequence> sequences = organization.get().getSequence();
            payrollStatus.add("All"); // Add "All" at the beginning
            payrollStatus.addAll(sequences.stream().map(Sequence::getRoleType).toList());
        }
        return payrollStatus;
    }
    @Override
    public List<PayrollPaysheetDTO> runPaySheet(List<String> type) throws ExecutionException, InterruptedException {

        boolean hasCommon = getStatus().stream().anyMatch(type::contains);
        String payrollStatus =  hasCommon ? type.stream().findFirst().get() : "Employee";
        List<String> employeeList = payrollStatus.equalsIgnoreCase("Employee") ? type : List.of();

        PayrollMonth payrollMonth = payrollMonthProvider.getPayrollMonth();
        if(payrollMonth == null)
            throw new ResponseStatusException(HttpStatus.NO_CONTENT, translator.toLocale(AppMessages.NO_DATA_FOUND));
        log.info("payrollMonth: {}", payrollMonth);
        log.info("formatted Month: {}", payrollMonth.getFormattedMonth());

        PaySheetMonthInfoDTO monthInfoDTO = PayrollMonthMapper.mapToPaySheetMonthInfo(payrollMonth);
        List<PayrollPaysheetDTO> result =  payrollPaysheetRepository.getPaySheetDetails(mongoTemplate, jwtHelper.getOrganizationCode(),payrollStatus, payrollMonth, employeeList );
        log.info("result -{}", result);
        if(result.isEmpty())
            throw new CustomResponseStatusException(AppMessages.PAYSHEET_RUN_EMPLOYEE_COUNT, HttpStatus.NOT_FOUND, new Object[]{0});
        log.info("Result: {}", result);
        ExecutorService executor = Executors.newCachedThreadPool();

        try {
            List<List<PayrollPaysheetDTO>> batches = splitIntoBatches(result);
            List<Future<Void>> futures = batches.stream()
                    .map(batch -> executor.submit((Callable<Void>) () -> {
                        processBatch(batch, monthInfoDTO); // Process each batch
                        return null;
                    }))
                    .toList();

            // Wait for all tasks to complete
            for (Future<Void> future : futures) {
                future.get(); // Ensures batch execution is completed
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace(); // Handle exceptions appropriately
        } finally {
            executor.shutdown();
        }


        return result;
    }
    private void processBatch(List<PayrollPaysheetDTO> batch, PaySheetMonthInfoDTO monthInfoDTO) {
        for (PayrollPaysheetDTO paysheet : batch) {
            System.out.println("Processing: " + paysheet);

            List<PayrollCTCBreakupsDTO> ctcBreakUps = paysheet.getCtcBreakUps();
            if(ctcBreakUps == null || ctcBreakUps.isEmpty()) {
                return;
            }
            int revisionCount = ctcBreakUps.size();
            PayrollCTCBreakupsDTO currentCTC = ctcBreakUps.get(0);
            PayrollCTCBreakupsDTO oldCTC = revisionCount > 1 ? ctcBreakUps.get(revisionCount-1) : new PayrollCTCBreakupsDTO();
            log.info("currentCTC - {}", currentCTC);
            log.info("oldCTC - {}", oldCTC);

            //Lop for this month
            LopDTO lopData = paysheet.getAttendanceMuster();
            float lop = lopData != null  ? lopData.getLop() : 0;
            float lopReversal = lopData != null  ? lopData.getLopReversal() : 0;
            float totalLop = lop - lopReversal;

            LocalDate doj = paysheet.getDoj().toLocalDate();
            LocalDate dol = paysheet.getDol() != null ? paysheet.getDol().toLocalDate() : null;
            log.info("working Information -{}, dol -{}", doj, dol);

            LocalDate payrollMonthStartDate = monthInfoDTO.getPayrollMonthStartDate();
            LocalDate payrollMonthEndDate = monthInfoDTO.getPayrollMonthEndDate();

            LocalDate startDate = doj.isAfter(payrollMonthStartDate) ? doj : payrollMonthStartDate;
            LocalDate endDate = dol != null && dol.isBefore(payrollMonthEndDate) ? dol : payrollMonthEndDate;
            log.info("startDate -{}, endDate -{}", startDate, endDate);
            int workDaysWithoutLop = (int) (ChronoUnit.DAYS.between(startDate,endDate) + 1);
            float workDaysWitLop =  workDaysWithoutLop - lop;
            log.info("Work Days -{}, work days with lop-{}", workDaysWithoutLop, workDaysWitLop);


        }
    }

    private List<List<PayrollPaysheetDTO>> splitIntoBatches(List<PayrollPaysheetDTO> list) {
        int totalSize = list.size();
        int numBatches = (int) Math.ceil((double) totalSize / 10);
        List<List<PayrollPaysheetDTO>> batches = new java.util.ArrayList<>(numBatches);

        for (int i = 0; i < totalSize; i += 10) {
            batches.add(list.subList(i, Math.min(i + 10, totalSize)));
        }
        return batches;
    }
}
