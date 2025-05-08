package com.hepl.budgie.entity.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemeList {

    private String title;
    private String schemeId;
    private long declaredAmount;
    private int ageRange;
    private long maxAmount;
    private String folderName;
    private String fileName;
    private String status;
    private String empRemarks;
    private String adminRemarks;
    private long approvedAmount;
    
}
