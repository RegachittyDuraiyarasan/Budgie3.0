package com.hepl.budgie.dto.payroll;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SchemesDTO {

    private String title;
    private String schemeId;
    private long amount;

}
