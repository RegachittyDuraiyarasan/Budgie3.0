package com.hepl.budgie.entity.leavemanagement;



import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveBalanceSummary {

	private String leaveTypeId;
	private String leaveTypeName;
	private double openingBalance;
	private double granted;
	private double availed;
	private double lapsed;
	private double balance;
	private int status;	
}
