package com.hepl.budgie.dto.leave;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HolidayApplyDto {
    
    private String holidayId;
    private String appliedTo;
    private List<String> cc; 
    private String reason;
}
