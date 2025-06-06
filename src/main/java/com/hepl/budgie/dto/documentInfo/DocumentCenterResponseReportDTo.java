package com.hepl.budgie.dto.documentInfo;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.enums.DocumentCenterReportEnum;
import com.hepl.budgie.enums.DocumentCenterReportProcessEnum;

import lombok.Data;

@Data
public class DocumentCenterResponseReportDTo {
    @ValueOfEnum(enumClass = DocumentCenterReportProcessEnum.class, message = "{validation.error.invalid}")
    private String process;
    @ValueOfEnum(enumClass = DocumentCenterReportEnum.class, message = "{validation.error.invalid}")
    private String status;
    private String category;
}
