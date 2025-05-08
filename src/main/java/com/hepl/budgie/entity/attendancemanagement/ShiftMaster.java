package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "m_shift")
@NoArgsConstructor
@AllArgsConstructor
public class ShiftMaster {

	@Id
	private String id;
	private String shiftCode;
	private String shiftName;
	private String inTime;
	private String outTime;
	private List<BreakTime> breakTime;
	private String halfDayTime;
	private String createdBy;
	private LocalDateTime createdAt;
	private String updatedBy;
	private LocalDateTime updatedAt;
	private boolean status;
}
