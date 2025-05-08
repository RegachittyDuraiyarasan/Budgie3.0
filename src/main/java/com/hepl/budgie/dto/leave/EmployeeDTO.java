package com.hepl.budgie.dto.leave;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class EmployeeDTO {

	private String empId;
	private String name;
	private String firstName;
	private String department;
	private String designation;
	private String workLocation;
	private String yearMonth;
	private String dateOfJoining;

}
