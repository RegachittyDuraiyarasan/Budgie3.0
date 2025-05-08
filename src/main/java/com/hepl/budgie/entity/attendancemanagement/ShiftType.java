package com.hepl.budgie.entity.attendancemanagement;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Document(collection = "shift_type")
@NoArgsConstructor
@AllArgsConstructor
public class ShiftType {

	@Id
	private String id;
	private String shiftType;
	private String createdBy;
    private String updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean status;
}
