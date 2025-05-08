package com.hepl.budgie.dto.attendancemanagement;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PunchPair {

    private LocalDateTime inTime;
    private LocalDateTime outTime;
    
}
