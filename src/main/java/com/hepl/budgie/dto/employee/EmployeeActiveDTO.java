package com.hepl.budgie.dto.employee;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeActiveDTO {
    private String employeeName;
    private String empId;
    private Integer years;
    private Integer months;
    private Integer days;
}
