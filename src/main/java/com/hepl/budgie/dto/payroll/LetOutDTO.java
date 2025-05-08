package com.hepl.budgie.dto.payroll;

import java.util.List;

import com.hepl.budgie.config.annotation.ItLetOutValidation;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ItLetOutValidation
public class LetOutDTO {

    @Valid
    private ItLetOutDTO itLetOut;
    private List<@Valid ItLetOutPropertiesDTO> itLetOutProperties;

}
