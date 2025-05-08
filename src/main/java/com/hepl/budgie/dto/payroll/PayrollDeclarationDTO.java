package com.hepl.budgie.dto.payroll;

import java.util.List;

import com.hepl.budgie.entity.payroll.PreviousEmploymentTax;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayrollDeclarationDTO {

    private boolean showDeclaration;
    private boolean showModel;
    private String message;
    private List<PayrollTypeDTO> schemes;
    private PreviousEmploymentTax previousEmploymentTax;

}
