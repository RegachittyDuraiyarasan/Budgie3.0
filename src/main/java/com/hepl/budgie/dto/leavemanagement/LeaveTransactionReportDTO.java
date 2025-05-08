package com.hepl.budgie.dto.leavemanagement;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LeaveTransactionReportDTO {

	@JsonProperty("leaveType")
	private String leaveTypeName; 

	@JsonProperty("transactionType")
	private String transactionType;

	@JsonProperty("fromDate")
	private String fromDate;

	@JsonProperty("toDate")
	private String toDate;

	@JsonProperty("sortBy")
	private String sortBy;
	
	@JsonProperty("generateAs")
	private String generateAs;
	
	@JsonProperty("leaveScheme")
	private String leaveScheme;
}
