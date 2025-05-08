package com.hepl.budgie.dto.leavemanagement;

import lombok.Data;

@Data
public class LockAttendanceDTO {
    private String id;
    private String standardStartDate;
    private String standardEndDate;
    private String attendanceLockDate;
    private String attendanceEmpLockDate;
    private String attendanceRepoLockDate;
    private String orgId;
}
