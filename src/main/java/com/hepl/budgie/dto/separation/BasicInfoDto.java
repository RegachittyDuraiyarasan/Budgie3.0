package com.hepl.budgie.dto.separation;

import java.time.LocalDate;

import lombok.Data;

@Data
public class BasicInfoDto {
private String _id;
private String empId;
private String firstName;
private String lastName;
private String designation;
private LocalDate appliedDate;
private String joiningDate;
private String noticePeriod;
private String department;
private LocalDate relievingDate;
}
