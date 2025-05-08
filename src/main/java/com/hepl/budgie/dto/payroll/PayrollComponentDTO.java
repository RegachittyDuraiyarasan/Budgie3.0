package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.payroll.payrollEnum.ComponentType;
import com.hepl.budgie.entity.payroll.payrollEnum.PayType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PayrollComponentDTO {
    private String componentId;
    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = ComponentType.class, message = "{validation.error.invalid}")
    private String componentType;
    @NotBlank(message = "{validation.error.notBlank}")
    private String componentName;
    private String componentSlug;
    @NotBlank(message = "{validation.error.notBlank}")
    @ValueOfEnum(enumClass = PayType.class, message = "{validation.error.invalid}")
    private String payType;
    @NotBlank(message = "{validation.error.notBlank}")
    private String compNamePaySlip;
    @NotNull(message = "{validation.error.notBlank}")
    private Boolean proDataBasisCalc;
    @NotNull(message = "{validation.error.notBlank}")
    private Boolean arrearsCalc;
    @NotNull(message = "{validation.error.notBlank}")
    private Boolean compShowInPaySlip;
    private String status = Status.ACTIVE.label;
}
