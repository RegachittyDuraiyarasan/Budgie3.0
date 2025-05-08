package com.hepl.budgie.dto.leavemanagement;

import java.util.Date;

import lombok.Data;

@Data
public class EmployeeLeaveBalanceReportDTO {

	private String fromDate;

	private String toDate;

	private String leaveScheme;

}
