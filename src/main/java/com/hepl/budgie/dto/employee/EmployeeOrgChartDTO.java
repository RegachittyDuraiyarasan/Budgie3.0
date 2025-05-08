package com.hepl.budgie.dto.employee;

import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hepl.budgie.entity.FilePathStruct;
import com.hepl.budgie.entity.userinfo.IdCardDetails;

import lombok.Data;

@Data
public class EmployeeOrgChartDTO {

    private String empId;
    private String name;
    private String image;
    private String designation;
    private String department;
    private String email;
    private String workLocation;
    private String dateOfJoining;
    private String dateOfBirth;
    private String currentDateOfJoining;
    private String currentDateOfBirth;
    private String primaryContactNumber;
    private int direct;
    private int subsidiaries;
    private String otherExperience;
    private String experience;
    private FilePathStruct profile;
    private FilePathStruct banner;
    private String organization;
    private boolean mailSent;
    private IdCardDetails idcarddetails;
    @JsonIgnore
    private int years;
    @JsonIgnore
    private int days;
    @JsonIgnore
    private int months;
    private List<EmployeeOrgChartDTO> children = Collections.emptyList();

}
