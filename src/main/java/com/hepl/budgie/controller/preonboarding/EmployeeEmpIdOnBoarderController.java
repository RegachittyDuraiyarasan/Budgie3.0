package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeCreateDTO;
import com.hepl.budgie.service.preonboarding.EmployeeEmpIdOnBoarderService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/newEmpIdsCreation")
@RequiredArgsConstructor
public class EmployeeEmpIdOnBoarderController {
    private final EmployeeEmpIdOnBoarderService employeeEmpIdOnBoarderService;

    private final Translator translator;

    @PutMapping()
    public GenericResponse<List<String>> updateMultipleEmployeeEmpIds(@RequestBody List<String> empIds) {
        List<String> result = employeeEmpIdOnBoarderService.updateMultipleEmployeeEmpIds(empIds);
        return GenericResponse.success(result);
    }
    @PostMapping("/empId/{empId}")
    public GenericResponse updateOnboardingStatus (@PathVariable String empId){
        employeeEmpIdOnBoarderService.updateOnboardingStatus(empId);

        return GenericResponse.success(translator.toLocale(AppMessages.ONBOARDING_INFO));
    }
     @PutMapping("/newEmpId")
    public GenericResponse<String> updateNapsAndNatsEmpId(@RequestBody EmployeeCreateDTO employeeCreateDTO) {
        String result = employeeEmpIdOnBoarderService.updateNapsAndNatsEmpId(employeeCreateDTO);
        return GenericResponse.success(result);
    }
}
