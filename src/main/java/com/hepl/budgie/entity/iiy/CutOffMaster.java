package com.hepl.budgie.entity.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hepl.budgie.config.auditing.AuditInfo;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@Document(collection = "cut_off_master")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CutOffMaster extends AuditInfo {
    @Id
    private String id;
    @NotBlank(message = "Financial Year is required")
    private String financialYear;
    @NotBlank(message = "StartDate is required")
    private String startDate;
    @NotBlank(message = "StartDate is required")
    private String endDate;
    @Builder.Default
    private int status = 1;
    @Builder.Default
    private int delete = 0;

}
