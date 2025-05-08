package com.hepl.budgie.entity.pms;

import lombok.Data;

@Data
public class PmsEmployeeDetails {
    private String empName;
    private String repManagerId;
    private String repManagerName;
    private String reviewerId;
    private String reviewerName;
    private String divisionHeadId;
    private String divisionHeadName;
    private String businessHeadId;
    private String businessHeadName;
    private String hrBpManagerId;
    private String hrBpManagerName;
    private String designation;
    private String department;
    private String repManDept;
    private String reviewerDept;
    private String hrBpDept;
    private String grade;
    private String roleOfIntake;
    private String dateOfJoining;
    private String groupOfJoining;
    private String pmsEligibleStatus;
    private String ctc;
    private String domain;
    private String manPower;
}
