package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollLoanDTO;
import com.hepl.budgie.entity.payroll.PayrollLoan;
import com.hepl.budgie.repository.payroll.PayrollLoanRepository;
import com.hepl.budgie.service.payroll.PayrollLoanService;
import com.hepl.budgie.utils.AppUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayrollLoanServiceImpl implements PayrollLoanService {
    private final PayrollLoanRepository payrollLoanRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<PayrollLoan> fetch() {
        return payrollLoanRepository.findByLoans(mongoTemplate, jwtHelper.getOrganizationCode());
    }

    @Override
    public String add(String save, PayrollLoanDTO request) {
        boolean exist = payrollLoanRepository.findByEmpIdAndBalanceisZero(request.getEmpId(), mongoTemplate, jwtHelper.getOrganizationCode());
        if(exist)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee loan balance are pending");
        int emiAmount = request.getLoanAmount() / request.getNoOfInstallments();

        List<PayrollLoan.LoanDetails> installmentDetails = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        LocalDate startDate = request.getBeginMonth().toInstant()
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate();

        for (int i = 1; i <= request.getNoOfInstallments(); i++) {
            PayrollLoan.LoanDetails installment = new PayrollLoan.LoanDetails();
            String formattedMonth = startDate.format(formatter);

            installment.setPayrollMonth(formattedMonth);
            installment.setAmount(emiAmount);
            installmentDetails.add(installment);
            startDate = startDate.plusMonths(1);
        }

        PayrollLoan payrollLoan = getLoan(request, emiAmount, installmentDetails);
        log.info("Details - {}",payrollLoanRepository.findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate));
        payrollLoan.setLoanId(payrollLoanRepository
                .findLatestComponent(jwtHelper.getOrganizationCode(), mongoTemplate)
                .map(e-> AppUtils.generateUniqueId(e.getLoanId()))
                .orElse("L00001"));
        payrollLoanRepository.saveLoan(payrollLoan, mongoTemplate, jwtHelper.getOrganizationCode());

        log.info("Loan details saved successfully: {}", payrollLoan);
        return "Loan details added successfully.";
    }

    private static PayrollLoan getLoan(PayrollLoanDTO request, int emiAmount, List<PayrollLoan.LoanDetails> installmentDetails) {
        PayrollLoan payrollLoan = new PayrollLoan();
        payrollLoan.setEmpId(request.getEmpId());
        payrollLoan.setLoanName(request.getLoanName());
        payrollLoan.setLoanType(request.getLoanType());
        payrollLoan.setLoanAmount(request.getLoanAmount());
        payrollLoan.setNoOfInstallments(request.getNoOfInstallments());
        payrollLoan.setEmiAmount(emiAmount);
        payrollLoan.setBeginMonth(request.getBeginMonth());
        payrollLoan.setBalanceAmount(request.getLoanAmount());
        payrollLoan.setInstallmentDetails(installmentDetails);
        return payrollLoan;
    }

    @Override
    public String update() {
        return null;
    }

    @Override
    public String status() {
        return null;
    }

    @Override
    public String delete() {
        return null;
    }
}
