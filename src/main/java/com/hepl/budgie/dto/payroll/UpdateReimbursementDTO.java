package com.hepl.budgie.dto.payroll;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateReimbursementDTO {
    @NotBlank(message = "{validation.error.notBlank}")
    private String fbpType;
    @NotBlank(message = "{validation.error.notBlank}")
    private String billDate;
    @NotBlank(message = "{validation.error.notBlank}")
    private String billNo;
    @NotBlank(message = "{validation.error.notBlank}")
    @Positive(message = "{validation.error.positive}")
    private String billAmount;
    @NotNull(message = "{validation.error.notBlank}")
    private MultipartFile attachment;
}
