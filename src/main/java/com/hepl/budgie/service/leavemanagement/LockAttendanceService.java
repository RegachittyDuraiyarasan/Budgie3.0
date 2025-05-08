package com.hepl.budgie.service.leavemanagement;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.leavemanagement.LockAttendanceDTO;
import com.hepl.budgie.entity.payroll.PayrollLockMonth;

import java.util.List;

public interface LockAttendanceService {
    List<LockAttendanceDTO> getAttendanceDateList();

    void updateLockAttendanceDate(String id, FormRequest formRequest);

    String lockAttendance(String attendanceEmpLockDate, String attendanceRepoLockDate, String org);

    String lockDateUpdate(String id, String attendanceEmpLockDate, String attendanceRepoLockDate, String org);

}
