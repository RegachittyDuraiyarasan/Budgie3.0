package com.hepl.budgie.mapper.gradeMaster;

import com.hepl.budgie.dto.grade.GradeFetchDTO;
import com.hepl.budgie.entity.master.GradeMaster;
import com.hepl.budgie.entity.master.ProbationDetail;
import org.mapstruct.*;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface GradeMapper {

    @Mapping(target = "gradeId", source = "gradeId", qualifiedByName = "mapObjectToString")
    @Mapping(target = "gradeName", source = "gradeName", qualifiedByName = "mapObjectToString")
    @Mapping(target = "noticePeriod", source = "noticePeriod", qualifiedByName = "mapToInteger")
    @Mapping(target = "status", source = "status", qualifiedByName = "mapObjectToString")
    @Mapping(target = "probationStatus", source = "probationStatus", qualifiedByName = "mapToBoolean")
    @Mapping(target = "probationDetail", expression = "java(mapProbationDetail(grade))")
    GradeMaster toEntity(Map<String, Object> grade);

    @Named("mapObjectToString")
    default String mapObjectToString(Object value) {
        return value == null ? null : value.toString();
    }

    @Named("mapToInteger")
    default Integer mapToInteger(Object value) {
        if (value instanceof Number) {
            return ((Number) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Named("mapToBoolean")
    default Boolean mapToBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    @Named("mapProbationDetail")
    default ProbationDetail mapProbationDetail(Object value) {
        if (value instanceof Map<?, ?> probationDetailMap) {
            Integer defaultDurationMonth = mapToInteger(probationDetailMap.get("defaultDurationMonth"));
            Object extendedOptionMonthObj = probationDetailMap.get("extendedOptionMonth");

            if (extendedOptionMonthObj instanceof String) {
                return new ProbationDetail(defaultDurationMonth, List.of((String) extendedOptionMonthObj));
            } else if (extendedOptionMonthObj instanceof List<?>) {
                return new ProbationDetail(defaultDurationMonth, (List<String>) extendedOptionMonthObj);
            }
            return new ProbationDetail(defaultDurationMonth, null);
        }
        return null;
    }

    GradeFetchDTO mapToDTO(GradeMaster gradeMaster);


}
