package com.hepl.budgie.dto.form;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hepl.budgie.config.jackson.ForceStringDeserializer;

import com.hepl.budgie.entity.workflow.WorkFlow;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public class FormDTO {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private String id;

    @NotBlank(message = "{validation.error.notBlank}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String formName;

    @NotBlank(message = "{validation.error.notBlank}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    private String formType;

    private ApiFlow apiFlow;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private Map<String, Object> initialValue;

    @NotEmpty(message = "{validation.error.notBeEmpty}")
    private List<@Valid FormFieldsDTO> formFields;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private List<FormFieldsDTO> buttons;

    private List<WorkFlow> workflow;

}
