package com.hepl.budgie.mapper;

import org.mapstruct.AfterMapping;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.hepl.budgie.entity.leavemanagement.LeaveScheme;

import java.util.Map;
import java.util.Objects;

@Mapper(componentModel = "spring", unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface LeaveSchemeMapper {

	@Mapping(target = "id", ignore = true)
	LeaveScheme toEntity(Map<String, Object> leaveSchemeDTO);

	default String map(Object value) {
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	@AfterMapping
	default void validateSchemeName(@MappingTarget LeaveScheme leaveScheme, Map<String, Object> dto) {
		Object schemeName = dto.get("schemeName");

		if (Objects.isNull(schemeName) || schemeName.toString().trim().isEmpty()) {
			throw new IllegalArgumentException("Scheme name cannot be null or empty");
		}
	}
}
