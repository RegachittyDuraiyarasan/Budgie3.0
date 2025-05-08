package com.hepl.budgie.dto.attendancemanagement;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MusterHistoryDeleteDto {
    
    private String empId;
    @Schema(description = "yyyy-MM")
    private String yearMonth;
    @Schema(description = "yyyy-MM-dd")
    private String attendanceDate;
}
