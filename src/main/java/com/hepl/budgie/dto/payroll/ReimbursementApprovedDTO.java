package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.config.annotation.ValidReimbursement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
@ValidReimbursement
public class ReimbursementApprovedDTO {

    @NotNull(message = "{validation.error.notBlank}")
    private Double approvedBillAmount;
    @NotNull(message = "{validation.error.notBlank}")
    @Min(value = 0, message = "{validation.error.statusMustBeZeroOrOne}")
    @Max(value = 1, message = "{validation.error.statusMustBeZeroOrOne}")
    private Integer status;
    private String remark;
}
