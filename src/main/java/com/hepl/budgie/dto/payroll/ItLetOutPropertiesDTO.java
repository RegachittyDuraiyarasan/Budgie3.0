package com.hepl.budgie.dto.payroll;

import java.time.LocalDate;

import com.hepl.budgie.config.annotation.ValidItLetOutProperties;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidItLetOutProperties
public class ItLetOutPropertiesDTO {

    private long letableAmount;
    private long municipalTax;
    private long unrealizedTax;
    private long loanInterest;
    private String lenderName;
    @NotBlank(message = "Landlord PAN is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "{validation.pan}")
    private String lenderPan;
    private LocalDate availableDate;
    private LocalDate dateOfAcquisition;
       
}
