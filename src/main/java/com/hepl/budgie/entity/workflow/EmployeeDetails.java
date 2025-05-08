package com.hepl.budgie.entity.workflow;

import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Data
@RequiredArgsConstructor
public class EmployeeDetails {
    private String empId;
    private List<String> roleType;
}
