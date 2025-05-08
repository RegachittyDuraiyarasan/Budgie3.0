package com.hepl.budgie.entity.userinfo;

import lombok.Data;

import java.util.List;
@Data
public class PayrollDetails {
    private String pfCode;
//    private String payrollStatus;
    private String state;
    private String costCenter;
    private List<String> costCenterHistory;
    private String billingType;
    private String resourceType;
    private String division;
    private String metro;

}
