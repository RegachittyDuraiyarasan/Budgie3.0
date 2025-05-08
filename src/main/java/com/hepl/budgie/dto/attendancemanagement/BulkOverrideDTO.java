package com.hepl.budgie.dto.attendancemanagement;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkOverrideDTO {

    private String date;
    private String shiftCode;
    private String firstIn;
    private String lastOut;
    private String session1;
    private String session2;
    private String tittle;
    
}
