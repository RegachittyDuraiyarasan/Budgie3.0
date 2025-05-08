package com.hepl.budgie.mapper.movement;

import com.hepl.budgie.dto.movement.EmployeeCurrentDetail;
import org.bson.Document;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.Arrays;

@Mapper(componentModel = "spring")
public interface EmployeeDetailsMapper {

    @Mapping(target = "supervisorOld", expression = "java(getNestedField(document, \"sections.hrInformation.primary.managerId\"))")
    @Mapping(target = "reviewerOld", expression = "java(getNestedField(document, \"sections.hrInformation.reviewer.managerId\"))")
    @Mapping(target = "departmentOld", expression = "java(getNestedField(document, \"sections.workingInformation.department\"))")
    @Mapping(target = "designationOld", expression = "java(getNestedField(document, \"sections.workingInformation.designation\"))")
    EmployeeCurrentDetail toEmployeeDetailsDTO(Document document);

    default String getNestedField(Document document, String key) {
        return document != null ? document.getEmbedded(Arrays.asList(key.split("\\.")), String.class) : null;
    }
}
