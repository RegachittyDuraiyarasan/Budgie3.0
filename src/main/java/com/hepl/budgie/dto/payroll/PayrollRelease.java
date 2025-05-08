package com.hepl.budgie.dto.payroll;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollRelease{

    private String empId;
    private LocalDate endDate;

}
