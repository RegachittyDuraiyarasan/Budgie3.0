package com.hepl.budgie.dto.payroll;

import com.hepl.budgie.config.annotation.FileChecker;
import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.payroll.payrollEnum.VariablesType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class PayrollMonthlySuppImportDTO {
    @FileChecker(
            ext = "application/vnd.ms-excel, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            isMandatory = true,
            message = "{error.fileNotSupported}",
            allowedFormatArgs = ".xls, .xlsx"
    )
    private MultipartFile file;

    @ValueOfEnum(enumClass = VariablesType.class, message = "{validation.error.invalid}")
    private String type;
}
