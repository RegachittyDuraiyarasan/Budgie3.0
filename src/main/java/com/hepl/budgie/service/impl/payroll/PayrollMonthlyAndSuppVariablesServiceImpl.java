package com.hepl.budgie.service.impl.payroll;

import com.hepl.budgie.entity.payroll.PayrollMonthlyAndSuppVariables;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import com.hepl.budgie.repository.payroll.PayrollComponentRepository;
import com.hepl.budgie.repository.payroll.PayrollMonthlyVariableRepository;
import com.hepl.budgie.service.payroll.PayrollMonthlyAndSuppVariableService;
import com.hepl.budgie.utils.PayrollMonthProvider;
import com.mongodb.bulk.BulkWriteResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service()
@Slf4j
@RequiredArgsConstructor
public class PayrollMonthlyAndSuppVariablesServiceImpl implements PayrollMonthlyAndSuppVariableService {

    private final PayrollComponentRepository payrollComponentRepository;
    private final PayrollMonthProvider payrollMonthProvider;
    private final MongoTemplate mongoTemplate;
    private final PayrollMonthlyVariableRepository monthlyVariableRepository;

    public BulkWriteResult excelImport(List<Map<String, Object>> validRows, String type) {

        if (validRows.isEmpty()) {
            return BulkWriteResult.unacknowledged();
        }

        List<String> components = type.equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ?
                payrollComponentRepository.getActiveComponentsByOrgIdForExcel(mongoTemplate, getOrgCode()) : payrollComponentRepository.getVariableComponentsForExcel(mongoTemplate, getOrgCode());

        return monthlyVariableRepository.bulkSuppUpsert(mongoTemplate,
                getOrgCode(),
                validRows,
                type,
                payrollMonthProvider.getPayrollMonth().getPayrollMonth(),
                components);
    }

    public List<Map<String, Object>> list(String month, String variableType) {

        String payrollMonth =  month == null || month.isEmpty() ? payrollMonthProvider.getPayrollMonth().getPayrollMonth() : month;
        log.info("payrollMonth -{}", month == null);

        List<String> components = variableType.equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ?
                payrollComponentRepository.getActiveComponentsByOrgIdForExcel(mongoTemplate, getOrgCode()) : payrollComponentRepository.getVariableComponentsForExcel(mongoTemplate, getOrgCode());

        return monthlyVariableRepository.getByPayrollMonth(mongoTemplate, getOrgCode(), payrollMonth, variableType)
                .stream()
                .map(dt -> {
                    Map<String, Object> objectMap = new HashMap<>();

                    objectMap.put("empId", dt.getEmpId());
                    objectMap.put("empName", dt.getEmpName());
                    components.forEach(comp -> {
                        String component = comp.toLowerCase().replaceAll("_\\(.*\\)", "");
                        objectMap.put(component, dt.getComponentValues().getOrDefault(component, 0));
                    });

                    return objectMap;

                }).toList();


    }

    public boolean singleUpload(PayrollMonthlyAndSuppVariables monthlySuppVariablesDTO){

        monthlySuppVariablesDTO.setVariableType(VariablesType.MONTHLY_VARIABLE.getLabel());
        monthlySuppVariablesDTO.setPayrollMonth(payrollMonthProvider.getPayrollMonth().getPayrollMonth());
        log.info("Single upload Monthly variable Data -{}", monthlySuppVariablesDTO);

        return monthlyVariableRepository.upsert(monthlySuppVariablesDTO, mongoTemplate, getOrgCode());
    }

    public List<String> getHeaders(String variableType) {
        List<String> components = variableType.equalsIgnoreCase(VariablesType.SUPP_VARIABLE.label) ?
                payrollComponentRepository.getActiveComponentsByOrgIdForExcel(mongoTemplate, getOrgCode()) : payrollComponentRepository.getVariableComponentsForExcel(mongoTemplate, getOrgCode());
        return components.stream().map(comp -> {
            String component = comp.replaceAll("_\\(.*\\)", "");
            return component.replaceAll("_", " ");
        }).toList();

    }

    private String getOrgCode() {
        return "ORG00001";
//        return jwtHelper.getOrganizationCode();
    }

}
