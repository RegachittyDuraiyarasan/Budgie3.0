package com.hepl.budgie.dto.payroll;

import java.time.LocalDate;

import com.hepl.budgie.config.annotation.ValidDateRange;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@ValidDateRange(message = "{validation.date.range}")
public class PayrollHraDTO {

    @NotNull(message = "From date is required")
    private LocalDate from;

    @NotNull(message = "To date is required")
    private LocalDate to;

    @Positive(message = "Rent must be a positive value")
    private long rent;

    @NotBlank(message = "Landlord is required")
    private String landlord;

    @NotBlank(message = "House number is required")
    private String houseNumber;

    @NotBlank(message = "Street is required")
    private String street;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Pincode is required")
    private String pincode;

    @NotBlank(message = "Landlord name is required")
    private String landlordName;

    @NotBlank(message = "Landlord PAN is required")
    @Pattern(regexp = "[A-Z]{5}[0-9]{4}[A-Z]{1}", message = "{validation.pan}")
    private String landlordPan;

    @NotBlank(message = "Landlord house number is required")
    private String landlordHouseNumber;

    @NotBlank(message = "Landlord street is required")
    private String landlordStreet;

    @NotBlank(message = "Landlord city is required")
    private String landlordCity;

    @NotBlank(message = "Landlord pincode is required")
    private String landlordPincode;

}
