package com.hepl.budgie.dto.payroll;

import java.time.LocalDate;

import com.hepl.budgie.config.annotation.ValidItLetOut;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidItLetOut
public class ItLetOutDTO {

    private String name;
    private long declaredAmount;
    @NotBlank(message = "Landlord PAN is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "{validation.pan}")
    private String pan;
    private LocalDate dateofAvailing;
    private LocalDate dateOfAcquisition;

}
