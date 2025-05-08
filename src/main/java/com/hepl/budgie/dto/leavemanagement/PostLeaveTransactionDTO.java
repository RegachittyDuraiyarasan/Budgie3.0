package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class PostLeaveTransactionDTO {
	@JsonProperty("leaveType")
	private String leaveTypeName; 

	@JsonProperty("transactionType")
	private String transactionType;

	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("fromSession")
	private String fromSession;

	@JsonProperty("toDate")
	private String toDate;

	@JsonProperty("toSession")
	private String toSession;

	@JsonProperty("days")
	private double noOfDays;
}
