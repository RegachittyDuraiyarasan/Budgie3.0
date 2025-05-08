package com.hepl.budgie.dto.payroll;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.hepl.budgie.config.annotation.PTDeductionChecker;
import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.config.jackson.ForceStringDeserializer;
import com.hepl.budgie.entity.payroll.PayrollPt;
import com.hepl.budgie.entity.payroll.payrollEnum.DeductionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@PTDeductionChecker
public class PayrollPtDTO {

    private String ptId;
    @NotBlank(message = "{validation.error.notBlank}")
    private String state;
    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = DeductionType.class, message = "{validation.error.invalid}")
    private String periodicity;
    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = DeductionType.class, message = "{validation.error.invalid}")
    private String deductionType;
    private Object deductionMonDetails;
    @Valid
    private List<RangeDetails> rangeDetails;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RangeDetails {
        private String rangeId;
        @NotBlank(message = "{validation.error.notBlank}")
        private String gender;
        @NotNull(message = "{validation.error.notBlank}")
        @Positive(message = "Salary must be positive and Non-Zero values")
        private Integer salaryFrom;
        @NotNull(message = "{validation.error.notBlank}")
        @PositiveOrZero(message = "Salary must be positive")
        private Integer salaryTo;
        @NotNull(message = "{validation.error.notBlank}")
        private Integer taxAmount;
        private Boolean status = true;
    }
}
