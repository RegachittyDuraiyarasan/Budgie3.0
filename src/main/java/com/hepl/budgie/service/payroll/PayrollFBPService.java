package com.hepl.budgie.service.payroll;

import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.payroll.FbpCreatePlanDTO;
import com.hepl.budgie.dto.payroll.PayrollFBPCreatePlan;
import com.hepl.budgie.entity.payroll.PayrollFBPComponentMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPMaster;
import com.hepl.budgie.entity.payroll.PayrollFBPRange;

import java.util.List;
import java.util.Map;

public interface PayrollFBPService {
    void add(String func, PayrollFBPComponentMaster fbpComponent);

    void range(String func, PayrollFBPRange request);

    List<PayrollFBPRange> listRange();

    List<PayrollFBPComponentMaster> list();

    void addFBPMaster(String func, List<PayrollFBPMaster> request);

    List<PayrollFBPMaster> listMaster(String id);

    boolean updateFBPMaster(PayrollFBPMaster request);

    boolean status(String id);

    boolean deleteStatus(String id);

    boolean statusRange(String id);

    boolean deleteStatusRange(String id);

    boolean deleteStatusMaster(String id);

    boolean statusMaster(String id);

    void createPlan(List<PayrollFBPCreatePlan> empIds);

    List<FbpCreatePlanDTO> listPlan();

    void considerPlan(List<FbpCreatePlanDTO> empIds);

    List<EmployeeActiveDTO> employeeList();
    
    List<EmployeeActiveDTO> activeEmployeeList();

    List<EmployeeActiveDTO> considerEmployeeList();  
}
