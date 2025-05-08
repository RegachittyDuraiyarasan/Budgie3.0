package com.hepl.budgie.service.impl;

import com.hepl.budgie.entity.FinancialYear;
import com.hepl.budgie.repository.FinancialYearRepository;
import com.hepl.budgie.service.FinancialYearService;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class FinancialYearServiceImpl implements FinancialYearService {

    private final FinancialYearRepository financialYearRepository;

    public FinancialYearServiceImpl(FinancialYearRepository financialYearRepository) {
        this.financialYearRepository = financialYearRepository;
    }

    @Override
    public void add(FinancialYear request) {
        LocalDate startDateFormatter = LocalDate.parse(request.getStartMonthYear() + "-01");
        request.setEndMonthYear(startDateFormatter.plusMonths(11).format(DateTimeFormatter.ofPattern("yyyy-MM")));
        boolean exists = financialYearRepository.existsByCountry(request.getCountry());
        if (exists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.COUNTRY_ALREADY_FOUND);
        }
        financialYearRepository.save(request);
    }

    @Override
    public void list(List<FinancialYear> request) {

    }

    @Override
    public void update(FinancialYear request) {

    }

    @Override
    public void delete(String request) {

    }
}
