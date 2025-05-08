package com.hepl.budgie.dto.userinfo;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class WorkingInformationDTO {
    private String department;
    private String designation;
    private String doj;
    private String workLocation;
    private String roleOfIntake;
    private String ctc;
    private String grade;
    private String rfh;
    private String swipeMethod;
    private String officialEmail;
    private String candidateStatus;
    private String accessCardId;
    private String esiNo;
    private String groupOfDOJ;
    private ZonedDateTime dateOfRelieving;
    private String shift;
    private String weekOff;
    private String leaveScheme;
    private String marketFacingTitle;
    private String experience;
}
