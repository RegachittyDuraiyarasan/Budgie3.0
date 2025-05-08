package com.hepl.budgie.dto.preonboarding;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdateEmailRequestDTO {
    @NotBlank(message = "Employee ID cannot be blank")
    private String empId;

    @NotBlank(message = "Suggested email cannot be blank")
    @Email(message = "Invalid email format")
    private String suggestedMail;
}
