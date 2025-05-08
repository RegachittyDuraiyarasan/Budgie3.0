package com.hepl.budgie.dto.form;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.hepl.budgie.config.jackson.ForceStringDeserializer;
import com.hepl.budgie.entity.master.MasterFormDependencies;
import com.hepl.budgie.entity.settings.MasterFormSettings;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.AccessMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormFieldsDTO {

    @NotBlank(message = "{validation.error.notBlank}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Field reference from collection", accessMode = AccessMode.READ_WRITE)
    private String fieldId;

    @NotBlank(message = "{validation.error.notBlank}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Name to be shown on form", accessMode = AccessMode.READ_WRITE)
    private String fieldName;

    @NotBlank(message = "{validation.error.notBlank}")
    @JsonDeserialize(using = ForceStringDeserializer.class)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Input type", accessMode = AccessMode.READ_WRITE)
    private String type;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Grid position for web", accessMode = AccessMode.READ_WRITE)
    private Map<String, Integer> position;

    @JsonDeserialize(using = ForceStringDeserializer.class)
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Placeholder for input types", accessMode = AccessMode.READ_WRITE)
    private String placeholder;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Placeholder for input types", accessMode = AccessMode.WRITE_ONLY)
    private Object initialValue;

    @Schema(requiredMode = Schema.RequiredMode.NOT_REQUIRED, description = "Field reference from collection", accessMode = AccessMode.READ_WRITE)
    private ValidationDTO validation;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private Boolean show;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private Boolean required;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_ONLY)
    private Boolean disabled;

    @Schema(accessMode = AccessMode.READ_ONLY)
    private MasterFormSettings optionsReference;

    @Schema(accessMode = AccessMode.WRITE_ONLY)
    private MasterFormSettings optionsDefault;

    // Define options
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.WRITE_ONLY)
    private String optionsReferenceId;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String optionsReferenceLink;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String referenceName;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private String btnAction;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private List<@Valid MasterFormDependencies> dependencies;

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.READ_WRITE)
    private Attribute attribute;

    @NotEmpty(message = "{validation.error.notBeEmpty}")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, accessMode = AccessMode.WRITE_ONLY)
    private List<FieldAccessLevel> accessLevel;

    private Boolean multiple;

}
