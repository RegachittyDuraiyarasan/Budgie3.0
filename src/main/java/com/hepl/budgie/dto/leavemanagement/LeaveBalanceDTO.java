package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveBalanceDTO {

	private String leaveCode;
	private String leaveType;
	private Double openingBalance;
	private Double balance;
	private Double granted;
	private Double availed;
	private Double lapsed;
	private Double applied;
	private Integer status;
}
