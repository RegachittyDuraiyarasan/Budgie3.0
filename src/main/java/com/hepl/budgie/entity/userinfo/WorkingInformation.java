package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WorkingInformation {
    // private String division;
    private ZonedDateTime groupOfDOJ;
    private String mailIdRequired;
    private String department;
    private String designation;
    private String workLocation;
    private String roleOfIntake;
    private String ctc;
    private String cmt;
    private String rfh;
    private String swipeMethod;
    private String grade;
    private ZonedDateTime doj;
    private String manpowerOutsourcing;
    // private String shiftId;
    private String officialEmail;
    private String candidateStatus;
    private String accessCardId;
    private String esiNo;
    private String applyPF;
    private ZonedDateTime dateOfRelieving;
    private String shift;
    private String weekOff;
    private String leaveScheme;
    private String marketFacingTitle;
    private String payrollStatus;
    private String payrollStatusName;
    private String experience;

}
