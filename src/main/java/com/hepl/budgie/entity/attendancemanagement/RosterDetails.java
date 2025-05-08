package com.hepl.budgie.entity.attendancemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RosterDetails {

	private LocalDate date;
	private String shift;
	// private LocalDateTime createdAt;
	// private String createdBy;
	// private LocalDateTime updatedAt;
	// private String updatedBy;
}
