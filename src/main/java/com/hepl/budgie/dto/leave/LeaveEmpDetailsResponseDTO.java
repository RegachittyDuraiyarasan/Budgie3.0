package com.hepl.budgie.dto.leave;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class LeaveEmpDetailsResponseDTO {

	private String contactNo;
	private String empLockStartDate;
	private String empLockEndDate;
	private String startYear;
	private String endYear;
	private List<EmployeeDTO> applyingTo;
	private List<EmployeeDTO> cc;
}
