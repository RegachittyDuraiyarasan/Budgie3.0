package com.hepl.budgie.entity.payroll.object;

import com.hepl.budgie.entity.master.Actions;
import lombok.Data;
import java.util.List;

@Data
public class EmployerContribution {
    private String type;
    private Integer percentage;
    private Double fixedAmount;

    private String description;
}
