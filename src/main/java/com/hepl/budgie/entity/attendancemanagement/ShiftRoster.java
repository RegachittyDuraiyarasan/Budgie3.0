package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "attendance_shift_roster")
@NoArgsConstructor
@AllArgsConstructor
public class ShiftRoster{

	@Id
	private String id;
	private String empId;
	private String monthYear;
	List<RosterDetails> rosterDetails; 
	private String updatedBy;	
	private LocalDate updatedAt;	
}
