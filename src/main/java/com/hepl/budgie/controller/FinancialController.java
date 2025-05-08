package com.hepl.budgie.controller;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.entity.FinancialYear;
import com.hepl.budgie.service.FinancialYearService;
import com.hepl.budgie.utils.AppMessages;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/financial-year")
public class FinancialController {

    private final Translator translator;
    private final FinancialYearService financialYearService;

    public FinancialController(Translator translator, FinancialYearService financialYearService) {
        this.translator = translator;
        this.financialYearService = financialYearService;
    }

    @PostMapping()
    public GenericResponse<String> add(@Valid @RequestBody FinancialYear request) {
        log.info("Financial Year - {}", request);
        financialYearService.add(request);
        return GenericResponse.success(translator.toLocale(AppMessages.PAYROLL_ADD, new String[] {"Financial Year"}));
    }
}
