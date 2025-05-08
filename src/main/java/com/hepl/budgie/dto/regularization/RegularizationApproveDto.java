package com.hepl.budgie.dto.regularization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegularizationApproveDto {
    
    private String empId;
    private String regCode;
    private String action;  
    private String remark;
    private String month;
}
