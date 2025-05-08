
package com.hepl.budgie.dto.payroll;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;    

@Data   
@AllArgsConstructor
@NoArgsConstructor
public class PayrollRequestDTO {

    private List<PayrollRelease> payrollRelease;
    
}