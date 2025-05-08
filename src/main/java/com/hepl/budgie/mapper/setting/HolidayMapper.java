package com.hepl.budgie.mapper.setting;

import java.io.IOException;
import java.util.Base64;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.MappingTarget;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.dto.settings.HolidayDto;
import com.hepl.budgie.entity.settings.Holiday;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface HolidayMapper {

    @Mappings({
        @Mapping(target = "file", source = "file", qualifiedByName = "mapMultipartFileToString")
    })
    Holiday toHoliday(HolidayDto holidayDto);

    @Named("mapMultipartFileToString")
    default String mapMultipartFileToString(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Mappings({
        @Mapping(target = "file", source = "file", qualifiedByName = "mapMultipartFileToString")
    })
    Holiday toUpHolidays(HolidayDto holiday ,@MappingTarget Holiday existingHoliday);
    
}
