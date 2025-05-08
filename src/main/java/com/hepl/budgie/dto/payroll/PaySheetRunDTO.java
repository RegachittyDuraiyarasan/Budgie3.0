package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PaySheetRunDTO {
    @NotEmpty(message = "{validation.error.notBeEmpty}")
    private List<String> type;
}
