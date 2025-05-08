package com.hepl.budgie.utils;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.payroll.PayrollMonth;
import com.hepl.budgie.repository.payroll.PayrollLockMonthRepository;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class PayrollMonthProvider {
    private PayrollMonth payrollMonth;
    private final PayrollLockMonthRepository lockMonthRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    public PayrollMonthProvider(PayrollLockMonthRepository lockMonthRepository, MongoTemplate mongoTemplate, JWTHelper jwtHelper) {
        this.lockMonthRepository = lockMonthRepository;
        this.mongoTemplate = mongoTemplate;
        this.jwtHelper = jwtHelper;
    }
    public synchronized PayrollMonth getPayrollMonth() {
        if (payrollMonth == null) {
            payrollMonth = fetchPayrollMonth();
        }
        return payrollMonth;
    }

    private PayrollMonth fetchPayrollMonth() {
        return lockMonthRepository.getLockedPayrollMonth(mongoTemplate, getOrgCode(), "IN");
    }

    public synchronized void refreshPayrollMonth() {
        this.payrollMonth = fetchPayrollMonth();
    }
    private String getOrgCode(){
        return jwtHelper.getOrganizationCode();
//        return "ORG00001";
    }
}
