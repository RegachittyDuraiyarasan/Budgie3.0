package com.hepl.budgie.dto.leavemanagement;

import java.util.List;

import com.hepl.budgie.entity.leavemanagement.Details;

import lombok.Data;

@Data
public class LeaveGranterTableDTO {

	private int sNo;
	private String empId;
	private String employeeName;
	private String roleOfIntake;
	private String confirmed;
	private String dateOfJoin;
	private String scheme;
	private String periodicity;
	private String fromDate;
	private String toDate;
	private String postedOn;
	private List<Details> details;
}
