package com.hepl.budgie.entity.attendancemanagement;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BreakTime {

	private String name;
	private String startTime;
	private String endTime;
	private String description;
}
