package com.hepl.budgie.dto.employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.userinfo.IdCardDetails;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeDetailsDTO {
    private String empId;
    private String empName;
    private String status;
    private String gender;
    private String doj;
    private String dob;
    private String department;
    private String designation;
    private String workLocation;
    private String grade;
    private String officialEmail;
    private String personalEmail;
    private String contactNumber;
    private String reviewerName;
    private String reportingManagerName;
    private String groupDoj;
    private String roleOfIntake;
    private String swipeMethod;
    private List<String> accessType;
    private String attendanceFormat;
    private String bloodGroup;
    private OrganizationRef organization;
    private List<OrganizationRef> subOrganization;
    private String maritalStatus;
    private int noticePeriod;
    private String payrollStatusName;
    private IdCardDetails idcarddetails;
}
