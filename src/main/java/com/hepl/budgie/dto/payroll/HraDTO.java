package com.hepl.budgie.dto.payroll;

import java.util.List;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HraDTO {

    private List<@Valid PayrollHraDTO> hraDetails;
    
}
