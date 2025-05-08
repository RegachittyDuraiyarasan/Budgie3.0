package com.hepl.budgie.service.impl.excel;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.excel.HeaderList;
import com.hepl.budgie.dto.payroll.PayrollPayTypeCompDTO;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.service.excel.ExcelBuilder;
import com.hepl.budgie.service.excel.ExcelExport;
import com.hepl.budgie.utils.PayrollMonthProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Stream;

@RequiredArgsConstructor
@Slf4j
@Component
public class CTCBreakupSampleExcelExport implements ExcelExport {
    private final PayrollComponentRepository payrollComponentRepository;
    private final MongoTemplate mongoTemplate;
    private final PayrollMonthProvider payrollMonthProvider;
    private final JWTHelper jwtHelper;
    @Override
    public List<HeaderList> prepareHeaders(){

        List<HeaderList> headerList = new ArrayList<>(Stream.of(
                new HeaderList("Employee_ID", true, "String"),
                new HeaderList("With_Effect_Date", true, "Date")
        ).toList());
        List<String> componentType = List.of(ComponentType.EARNINGS.label, ComponentType.DEDUCTION.label);
        payrollComponentRepository.getFixedComponents(mongoTemplate, getOrgCode(), componentType)
                .forEach(payrollComponent -> headerList.add(new HeaderList(payrollComponent.getComponentName().replaceAll("\\s+", "_") + "_(Annual)", false, "int")));

        return  headerList;
    }

    @Override
    public List<ExcelBuilder.DropdownConfig> prepareDropdowns(){
        List<String> headers =  prepareHeaders().stream().map(HeaderList::getHeader).toList();
        int columnIndex = new ArrayList<>(headers)
                .indexOf("With_Effect_Date");
        List<ExcelBuilder.DropdownConfig> validation = new ArrayList<>();
        if(columnIndex != -1){
            String startDate = payrollMonthProvider.getPayrollMonth().getStartDate().toLocalDate().toString();
            String endDate = payrollMonthProvider.getPayrollMonth().getEndDate().toLocalDate().toString();
            validation.add(new ExcelBuilder.DropdownConfig(columnIndex, startDate, endDate));
        }
        List<String> headerList = new ArrayList<>(headers);
        for (int i = 0; i < headerList.size(); i++) {
            if (headerList.get(i).endsWith("(Annual)")) {
                validation.add(new ExcelBuilder.DropdownConfig(i, true, true));
            }
        }

        return validation;
    }
    private String getOrgCode(){
       return jwtHelper.getOrganizationCode();
//         return "ORG00001";
    }

}
