package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
@Component
@RequiredArgsConstructor
public class SuppVariableSampleExcelExport implements ExcelExport {
    private final PayrollComponentRepository payrollComponentRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public List<HeaderList> prepareHeaders(){

        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String")
        ).toList());

        payrollComponentRepository.getActiveComponentsByOrgIdForExcel(mongoTemplate, orgCode())
                .forEach(payrollComponent -> {
                    headerList.add(new HeaderList(payrollComponent, false, "int"));
                });

        return  headerList;
    }
    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns(){
        List<String> headers =  prepareHeaders().stream().map(HeaderList::getHeader).toList();
        List<String> headerList = new ArrayList<>(headers);
        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();
        for (int i = 0; i < headerList.size(); i++) {
            if (headerList.get(i).startsWith("Sup")) {
                validation.add(new ExcelBuilder.DropdownConfig(i, true,false));
            }
        }

        return validation;
    }
    private String orgCode(){
//        return jwtHelper.getOrganizationCode();
        return "ORG00001";
    }
}
