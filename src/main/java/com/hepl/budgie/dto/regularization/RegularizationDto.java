package com.hepl.budgie.dto.regularization;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegularizationDto {

    private List<RegularizationInfoDto> regularizationInfo; 
    private String appliedTo;
    private String remark;
    private String monthYear;
    
}
