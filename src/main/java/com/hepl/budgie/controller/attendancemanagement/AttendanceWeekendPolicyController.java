package com.hepl.budgie.controller.attendancemanagement;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.attendancemanagement.AttendanceWeekendDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.service.attendancemanagement.AttendanceWeekendPolicyService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Tag(name = "Attendance Weekend Policy")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance-weekend-policy")
public class AttendanceWeekendPolicyController {

    private final AttendanceWeekendPolicyService attendanceWeekendPolicyService;
    private final Translator translator;

    @PostMapping()
    public GenericResponse<AttendanceWeekendPolicy> saveWeekendPolicy(@RequestBody AttendanceWeekendDTO weekend) {
        
        AttendanceWeekendPolicy attendanceWeekendPolicy = attendanceWeekendPolicyService.saveWeekendPolicy(weekend);
        return GenericResponse.success(translator.toLocale(AppMessages.WEEKEND_POLICY_SAVED_SUCCESSFULLY), attendanceWeekendPolicy);
        
    }

    @GetMapping()
    public GenericResponse<List<AttendanceWeekendPolicy>> getWeekendPolicy() {
        
        List<AttendanceWeekendPolicy> attendanceWeekendPolicy = attendanceWeekendPolicyService.getWeekendPolicy();
        return GenericResponse.success(attendanceWeekendPolicy);
    }

    @GetMapping("/month")
    public GenericResponse<AttendanceWeekendPolicy> getWeekendPolicyByMonth(@RequestParam String month) {
        
        AttendanceWeekendPolicy attendanceWeekendPolicy = attendanceWeekendPolicyService.getWeekendPolicyByMonth(month);
        return GenericResponse.success(attendanceWeekendPolicy);
    }

    @PutMapping()
    public GenericResponse<AttendanceWeekendPolicy> updateWeekendPolicy(@RequestParam String month, @RequestBody AttendanceWeekendDTO weekend) {
        
        AttendanceWeekendPolicy attendanceWeekendPolicy = attendanceWeekendPolicyService.updateWeekendPolicy(month, weekend);
        return GenericResponse.success(translator.toLocale(AppMessages.WEEKEND_POLICY_UPDATED_SUCCESSFULLY), attendanceWeekendPolicy);
    }

    @GetMapping("/weekends")
    public GenericResponse<Map<String, Object>> getWeekends(@RequestParam String monthYear) {
        
        Map<String, Object> weekend = attendanceWeekendPolicyService.getWeekends(monthYear);
        return GenericResponse.success(weekend);
    }
    
   

    
}
