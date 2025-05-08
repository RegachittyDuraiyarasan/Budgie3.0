package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.service.excel.ExcelExport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component("MonthlyImportSample")
@Slf4j
@RequiredArgsConstructor
public class MonthlyImportSampleExcelExport implements ExcelExport {
    private final PayrollComponentRepository payrollComponentRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<HeaderList> prepareHeaders() {

        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String")
        ).toList());

        payrollComponentRepository.getVariableComponentsForExcel(mongoTemplate, orgCode())
                .forEach(comp -> headerList.add(new HeaderList(comp, false, "int")));

        return headerList;
    }

    private String orgCode() {
//        return jwtHelper.getOrganizationCode();
        return "ORG00001";
    }
}
