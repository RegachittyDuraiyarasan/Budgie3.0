package com.hepl.budgie.entity.payroll;

import java.time.ZonedDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollLetOut {

    private String name;
    private long declaredAmount;
    private String pan;
    private ZonedDateTime dateofAvailing;
    private ZonedDateTime dateOfAcquisition;

}
