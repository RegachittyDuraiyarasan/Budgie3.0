package com.hepl.budgie.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmpIdDTO {
    @NotBlank(message = "{validation.error.notBlank}")
    private String empId;
}
