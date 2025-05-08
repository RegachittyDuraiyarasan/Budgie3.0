package com.hepl.budgie.service;

import com.hepl.budgie.entity.FinancialYear;

import java.util.List;

public interface FinancialYearService {
    void add(FinancialYear request);
    void list(List<FinancialYear> request);
    void update(FinancialYear request);
    void delete(String request);
}
