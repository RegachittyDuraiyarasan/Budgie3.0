package com.hepl.budgie.dto.leave;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminLeaveApproveDto {
    
    private String empId;
    private String leaveCode;
    private String type;
    private String remark;
}
