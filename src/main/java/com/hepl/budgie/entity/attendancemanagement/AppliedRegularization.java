package com.hepl.budgie.entity.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppliedRegularization {

    private String attendanceDate;
    private String shift;
    private String startTime;
    private String endTime;
    private String actualInTime;
    private String actualOutTime;
    private String reason;
    private String status;
    private String approverRemarks;
    private String approvedDate;
    private String rejectedDate;
}
