package com.hepl.budgie.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LopDTO {

    private String empId;
    private String monthYear;
    private int lop;
    private int lopReversal;
    
}
