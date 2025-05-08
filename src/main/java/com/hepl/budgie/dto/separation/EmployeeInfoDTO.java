package com.hepl.budgie.dto.separation;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.entity.userinfo.BankDetails;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeInfoDTO {

    private String empId;
    private String firstName;
    private String lastName;
    private String middleName;
    private Integer noticePeriod;
    private String department;
    private String designation;
    private String dateOfJoining;
    private String appliedDate;
    private String relievingDate;
    private String reportingManager;
    private String reportingManagerName;
    private String reviewer;
    private String reviewerName;
    private String employeeNoDueStatus;
    private String resignationStatus;
    private String officalEmail;
    private String personalEmail;
    private String contactNumber;
    private List<BankDetails> bankDetails;
    private String uanNo;
    private String pfNo;
    private String remarks;
    private String reason;
 

}
