package com.hepl.budgie.controller.attendancemanagement;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.impl.xb.xsdschema.Public;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.regularization.RegularizationApproveDto;
import com.hepl.budgie.dto.regularization.RegularizationDto;
import com.hepl.budgie.entity.UserRef;
import com.hepl.budgie.entity.attendancemanagement.AttendanceInfo;
import com.hepl.budgie.entity.attendancemanagement.AttendanceRegularization;
import com.hepl.budgie.entity.leave.LeaveApply;
import com.hepl.budgie.service.attendancemanagement.AttendanceRegularizationService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@Tag(name = "Attendance Regularization")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/attendance-regularization")
public class AttendanceRegularizationController {

    private final AttendanceRegularizationService attendanceRegularizationService;
    private final JWTHelper jwtHelper;

    @PostMapping()
    public GenericResponse<AttendanceRegularization> applyAttendanceRegularization(@RequestBody RegularizationDto regularizationDto) throws MessagingException {
        UserRef ref = jwtHelper.getUserRefDetail();
        String empId = ref.getEmpId();                                                    
        AttendanceRegularization attendanceRegularization = attendanceRegularizationService.applyAttendanceRegularization(empId,regularizationDto);
        return GenericResponse.success(attendanceRegularization);

    }

    @PutMapping("/regularization-approved")
    public GenericResponse<AttendanceRegularization> approvedRegularization(
    @RequestParam String empId,
    @RequestParam String regCode,
    @RequestParam String key,
    @RequestParam(required = false) List<LocalDate> approvedDate,
    @RequestParam(required = false) List<LocalDate> rejectedDate,
    @RequestParam(required = false) String reason,
    @RequestParam(required = false) List<String> remarks,
    @RequestParam(required = false) String month
    ) throws MessagingException{
        AttendanceRegularization approved = attendanceRegularizationService.approvedRegularization(empId,regCode,key,approvedDate,rejectedDate,reason,remarks,month);
        return GenericResponse.success(approved);
    }

    @GetMapping("/all-regularization")
    public GenericResponse<Map<String, Object>> getRegulization(@RequestParam(required = false) String key) {
        Map<String, Object> result = attendanceRegularizationService.getRegulization(key);
        return GenericResponse.success(result);
    }
    

    @GetMapping("/emp-regularization")
    public GenericResponse<Map<String, Object>> getEmpRegulization(@RequestParam(required = false) String key){
        Map<String, Object> get = attendanceRegularizationService.getEmpRegulization(key);
        return GenericResponse.success(get);
    }

    @GetMapping("/regularization-quick-add")
    public GenericResponse<Map<String, Object>> getAbsentAttendance(
            @RequestParam String monthYear) {
                
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        Map<String, Object> response = attendanceRegularizationService.getAbsentAttendance(empId, monthYear);
        return GenericResponse.success(response);
    }

    @GetMapping("/absent-present")
    public GenericResponse<Map<String, Object>> getAbsentAndPresentAttendance(
            @RequestParam String currentMonth) {

        String empId = jwtHelper.getUserRefDetail().getEmpId();
        return attendanceRegularizationService.getAbsentAndPresentAttendance(empId, currentMonth);
    }

    @DeleteMapping("/withdrawal")
    public GenericResponse<String> withdrawal(@RequestParam String empId, @RequestParam String regCode, @RequestParam String monthYear){ 
        return attendanceRegularizationService.withdrawal(empId, regCode, monthYear);
    }

    @GetMapping("/get-applied-to")
    public GenericResponse<Map<String, Object>> getAppliedTo() {
        Map<String, Object> appliedTo = attendanceRegularizationService.getAppliedTo();
        return GenericResponse.success(appliedTo);
    } 
    
    @GetMapping("/get-admin-regularization")
    public GenericResponse<List<AttendanceRegularization>> getAdminRegularization(@RequestParam(required = false) String month,@RequestParam(required = false) String empId){ 
        List<AttendanceRegularization> adminRegularization = attendanceRegularizationService.getAdminRegularization(month,empId);
        return GenericResponse.success(adminRegularization);
    }

    @GetMapping("/get-admin-leave-apply")
    public GenericResponse<List<LeaveApply>> getAdminLeaveApply(@RequestParam(required = false) String leaveType,
    @RequestParam(required = false) String empId,
    @RequestParam(required = false) String month){
        
        List<LeaveApply> adminRegularization = attendanceRegularizationService.getAdminLeaveApply(leaveType,empId,month);
        return GenericResponse.success(adminRegularization);
    }  

    @PostMapping("/admin-approve-regularization")
    public GenericResponse<List<AttendanceRegularization>> adminApproveRegularization(@RequestBody List<RegularizationApproveDto> regularizationApproveDto) {
        List<AttendanceRegularization> adminRegularization = attendanceRegularizationService.adminApproveRegularization(regularizationApproveDto);
        return GenericResponse.success(adminRegularization);
    }

    @GetMapping("present-absent-list")
    public GenericResponse<Map<String, Object>> presentAbsentList(
            @RequestParam String currentMonth) {

        String empId = jwtHelper.getUserRefDetail().getEmpId();
        return attendanceRegularizationService.presentAbsentList(empId, currentMonth);
    }  
    
    @GetMapping("payroll-lock-date")
    public GenericResponse<Map<String, Object>> getPayrollLockDate(@RequestParam String currentMonth) {
        return attendanceRegularizationService.getPayrollLockDate(currentMonth);
    }
        
    
}
