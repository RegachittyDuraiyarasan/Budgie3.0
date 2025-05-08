package com.hepl.budgie.dto.leavemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeaveTypeInfoDTO {

	private String leaveUniqueCode;
	private String leaveTypeName;
	private String leaveTypeCode;

}
