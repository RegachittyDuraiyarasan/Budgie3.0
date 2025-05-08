package com.hepl.budgie.controller.leavemanagement;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LockAttendanceDTO;
import com.hepl.budgie.service.leavemanagement.LockAttendanceService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Manage Lock Attendance", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequestMapping("/leave/lock-attendance")
@Slf4j
public class LockAttendanceCtrl {
    private final LockAttendanceService lockAttendanceService;
    private final Translator translator;
    private final MasterFormService masterFormService;

    public LockAttendanceCtrl(LockAttendanceService lockAttendanceService, Translator translator,
            MasterFormService masterFormService) {
        this.lockAttendanceService = lockAttendanceService;
        this.translator = translator;
        this.masterFormService = masterFormService;
    }

    @GetMapping()
    @Operation(summary = "Get Lock and Attendance Date")
    public GenericResponse<List<LockAttendanceDTO>> fetch() {
        log.info("Lock and Attendance Date");
        return GenericResponse.success(lockAttendanceService.getAttendanceDateList());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update Lock Attendance Date")
    public GenericResponse<String> update(
            @PathVariable("id") String id,
            @RequestBody FormRequest formRequest) {
        log.info("Updating Lock and Attendance Date for ID: {}", id);
        lockAttendanceService.updateLockAttendanceDate(id, formRequest);
        return GenericResponse
                .success(translator.toLocale(AppMessages.PAYROLL_DELETE, new String[] { "Leave Scheme" }));
    }

    @PostMapping()
    public GenericResponse<String> lockAttendance(@RequestParam String attendanceEmpLockDate,@RequestParam String attendanceRepoLockDate,@RequestParam String org){
        return GenericResponse.success(lockAttendanceService.lockAttendance(attendanceEmpLockDate,attendanceRepoLockDate,org));
    }

    @PutMapping("/lock-date-update")
    public GenericResponse<String> lockDateUpdate(@RequestParam String id,
    @RequestParam String attendanceEmpLockDate,
    @RequestParam String attendanceRepoLockDate,
    @RequestParam String org){
        return GenericResponse.success(lockAttendanceService.lockDateUpdate(id,attendanceEmpLockDate,attendanceRepoLockDate,org));
    }

}
