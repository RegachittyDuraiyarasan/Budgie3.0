package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.entity.payroll.PayrollComponent;
import lombok.Data;

import java.util.List;

@Data
public class PayrollGroupedComponentDTO {
    private String id;
    private List<PayrollComponent> components;
}
