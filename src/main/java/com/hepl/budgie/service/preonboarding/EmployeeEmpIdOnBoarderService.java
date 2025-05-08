package com.hepl.budgie.service.preonboarding;

import com.hepl.budgie.dto.employee.EmployeeCreateDTO;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;

import java.util.List;

public interface EmployeeEmpIdOnBoarderService {

     List<String> updateMultipleEmployeeEmpIds(List<String> empIds);
     String updateNapsAndNatsEmpId(EmployeeCreateDTO employeeCreateDTO);
     OnBoardingInfo updateOnboardingStatus(String empId);
}
