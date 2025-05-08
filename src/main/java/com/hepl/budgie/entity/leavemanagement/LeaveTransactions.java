package com.hepl.budgie.entity.leavemanagement;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeaveTransactions {

	private String transactionId;
	private String leaveTypeId;
	private String leaveTypeName;
	private String transactionType;
	private String processedBy;
	private String postedOn;
	private String fromDate;
	private String toDate;
	private String fromSession;
	private String toSession;
	private double noOfDays;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
    private String createdByUser;
    private String modifiedByUser;
}
