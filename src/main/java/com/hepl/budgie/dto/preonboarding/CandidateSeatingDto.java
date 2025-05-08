package com.hepl.budgie.dto.preonboarding;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CandidateSeatingDto {
    @NotEmpty(message = "PreOnboarding type cannot be empty")
    private String preOnboarding;
    @NotNull(message = "Verified field cannot be empty")
    private Boolean verified;
    @NotNull(message = "Date cannot be null")
    private String date;
}
