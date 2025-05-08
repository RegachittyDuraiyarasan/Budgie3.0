package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HrInformation {
    private ReporteeDetail primary;
    private ReporteeDetail secondary;
    private ReporteeDetail reviewer;
    private ReporteeDetail recruiter;
    private ReporteeDetail divisionHead;
    private ReporteeDetail onboarder;
    private ReporteeDetail buddy;
    private Integer noticePeriod;
    private String attendanceFormat;
    private String weekOff;
    private String leaveScheme;
    private Boolean onboardingStatus;
    private Boolean inductionMailStatus;
    private Boolean buddyMailStatus;
    private boolean spocStatus;
    private boolean spocReportingManagerStatus;
    private String documentVerificationStatus;

}