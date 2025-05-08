package com.hepl.budgie.dto.leave;

import java.time.LocalDate;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class LeaveApplyDTO {

	private String empId;
//	@NotBlank(message = "Type is required")
//	@Pattern(regexp = "Employee|Reporting Manager|Admin", message = "Invalid type")
	private String type;
//	@NotBlank(message = "Leave Type is required")
	private String leaveType;
	private String appliedTo;
	private List<String> appliedCC;
//	@JsonFormat(pattern = "yyyy-MM-dd")
//	@NotNull(message = "From Date is Required")
	private LocalDate fromDate;
//	@JsonFormat(pattern = "yyyy-MM-dd")
//	@NotNull(message = "End Date is Required")
	private LocalDate toDate;
//	@NotBlank(message = "From Date Session is required")
	private String fromSession;
//	@NotBlank(message = "To Date Session is required")
	private String toSession;
	@Pattern(regexp = "^[0-9]{10}$", message = "Invalid contact number")
	private String contactNo;
	private List<MultipartFile> file;
//	@NotBlank(message = "Reason is required")
	private String reason;

	@AssertTrue(message = "End Date must be greater than or equal to Start Date")
	private boolean isValidEndDate() {
		if (fromDate == null || toDate == null) {
			return true; 
		}
		return !toDate.isBefore(fromDate);
	}

	@AssertTrue(message = "Invalid session combination: 'From Session' cannot be 'Session 2' when 'To Session' is 'Session 1' for the same date")
	private boolean isValidSessionCombination() {
		if (fromDate == null || toDate == null || fromSession == null || toSession == null) {
			return true; 
		}

		if (fromDate.equals(toDate)) {
			return !("Session 2".equalsIgnoreCase(fromSession) && "Session 1".equalsIgnoreCase(toSession));
		}
		return true;
	}
}
