package com.hepl.budgie.dto.userinfo;

import lombok.Data;

@Data
public class HRInfoDTO {
    private String empId;
    private String firstName;
    private String lastName;
    private String primaryManagerId;
    private String primaryManagerName;
    private String primaryManagerLastName;
    private String reviewerId;
    private String reviewerName;
    private String reviewerLastName;
    private String divisionHeadId;
    private String divisionHeadName;
    private String divisionHeadLastName;
    private String onboarderId;
    private String onboarderName;
    private String onboarderLastName;
    private String recruiterId;
    private String recruiterName;
    private String recruiterLastName;
    private String buddyId;
    private String buddyName;
    private String buddyLastName;
}
