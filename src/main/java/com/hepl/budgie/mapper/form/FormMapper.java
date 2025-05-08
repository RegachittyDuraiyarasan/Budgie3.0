package com.hepl.budgie.mapper.form;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.Map;
import java.util.stream.Stream;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.ValidationDTO;
import com.hepl.budgie.dto.formbuilder.FormBuilderFields;
import com.hepl.budgie.dto.formbuilder.Position;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterFormFields;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.utils.FileExtUtils;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FormMapper {

    @Mapping(target = "position", source = "styles.position", qualifiedByName = "mapPosition")
    @Mapping(target = "fieldId", source = "name")
    @Mapping(target = "fieldName", source = "label")
    @Mapping(target = "validation", source = "validations")
    @Mapping(target = "validation.views", ignore = true)
    @Mapping(target = "validation.format", ignore = true)
    @Mapping(target = "validation.disabledFuture", ignore = true)
    @Mapping(target = "validation.disabledPast", ignore = true)
    @Mapping(target = "validation.disabledDateRangeStart", ignore = true)
    @Mapping(target = "validation.disabledDateRangeEnd", ignore = true)
    @Mapping(target = "validation.disabledSpecificDates", ignore = true)
    @Mapping(target = "attribute.disableFuture", source = "validations.disabledFuture")
    @Mapping(target = "attribute.disablePast", source = "validations.disabledPast")
    @Mapping(target = "attribute.startFrom", source = "validations.disabledDateRangeStart")
    @Mapping(target = "attribute.endOn", source = "validations.disabledDateRangeEnd")
    @Mapping(target = "attribute.disableSpecificDates", source = "validations.disabledSpecificDates")
    @Mapping(target = "optionsDefault", source = "advanced.options", qualifiedByName = "mapDefaultOptions")
    @Mapping(target = "attribute.views", source = "advanced.views")
    @Mapping(target = "attribute.format", source = "advanced.dateFormat")
    FormFieldsDTO toFormFields(FormBuilderFields fields);

    List<FormFieldsDTO> toFormFields(List<FormBuilderFields> fields);

    @Mapping(target = "styles.position", source = "position")
    @Mapping(target = "name", source = "fieldId")
    @Mapping(target = "label", source = "fieldName")
    @Mapping(target = "advanced.options", source = "optionsDefault", qualifiedByName = "mapDefaultOptionsCommaSeparated")
    @Mapping(target = "validations", source = ".", qualifiedByName = "mapValidation")
    FormBuilderFields toFormBuilderField(MasterFormFields formFields);

    List<FormBuilderFields> toFormBuilderFields(List<MasterFormFields> fields);

    @Named("mapValidation")
    default ValidationDTO mapValidation(MasterFormFields fields) {
        return ValidationDTO.builder().disabledFuture((Boolean) fields.getAttribute().get("disableFuture"))
                .disabledPast((Boolean) fields.getAttribute().get("disabledPast"))
                .disabledDateRangeStart((String) fields.getAttribute().get("startFrom"))
                .disabledDateRangeEnd((String) fields.getAttribute().get("endOn"))
                .disabledSpecificDates((String) fields.getAttribute().get("disableSpecificDates"))
                .inputType(fields.getValidation().getInputType())
                .pattern(fields.getValidation().getPattern())
                .minLength(fields.getValidation().getMinLength())
                .maxLength(fields.getValidation().getMaxLength())
                .maxFileSize(fields.getValidation().getMaxFileSize())
                .maxFiles(fields.getValidation().getMaxFiles())
                .fileType(FileExtUtils.getExtensionsByMimeType(fields.getValidation().getFileType())).build();
    }

    @Named("mapDefaultOptions")
    default MasterFormSettings mapDefaultOptions(String options) {
        if (options != null) {
            return MasterFormSettings
                    .builder().options(Stream.of(options.split(","))
                            .map(option -> MasterFormOptions.builder().name(option).value(option)
                                    .status(Status.ACTIVE.label).build())
                            .toList())
                    .build();
        } else {
            return null;
        }

    }

    @Named("mapDefaultOptionsCommaSeparated")
    default String mapDefaultOptionsCommaSeparated(MasterFormSettings options) {
        if (options != null) {
            return options.getOptions().stream().map(MasterFormOptions::getName).collect(Collectors.joining(","));
        } else {
            return null;
        }

    }

    @SuppressWarnings("unchecked")
    @Named("mapPosition")
    default Map<String, Integer> getPositions(Position position) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(position, Map.class);
    }

}
