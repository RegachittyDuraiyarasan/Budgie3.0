package com.hepl.budgie.entity.attendancemanagement;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "m_attendance_day_type")
@NoArgsConstructor
@AllArgsConstructor
public class DayType {

	@Id
	private String id;
	private String dayTypeId;
	private String dayType;
	private boolean status;
	private boolean isActive;
	private String createdAt;
	private String createdBy;
	private String updatedAt;
	private String updatedBy;
}
