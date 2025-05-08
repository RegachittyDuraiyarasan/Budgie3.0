package com.hepl.budgie.mapper.userinfo;
import com.hepl.budgie.dto.userinfo.ContactDTO;
import com.hepl.budgie.entity.userinfo.Contact;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)

public interface ContactMapper {

    @Mapping(target = "permanentAddressDetails.permanentAddress", source = "permanentAddress")
    @Mapping(target = "permanentAddressDetails.permanentState", source = "permanentState")
    @Mapping(target = "permanentAddressDetails.permanentDistrict", source = "permanentDistrict")
    @Mapping(target = "permanentAddressDetails.permanentTown", source = "permanentTown")
    @Mapping(target = "permanentAddressDetails.permanentPinZipCode", source = "permanentPinZipCode")

    @Mapping(target = "presentAddressDetails.presentAddress", source = "presentAddress")
    @Mapping(target = "presentAddressDetails.presentState", source = "presentState")
    @Mapping(target = "presentAddressDetails.presentDistrict", source = "presentDistrict")
    @Mapping(target = "presentAddressDetails.presentTown", source = "presentTown")
    @Mapping(target = "presentAddressDetails.presentPinZipCode", source = "presentPinZipCode")

    // note: source is given the inputs like permanentAddress, target is how to insert the data with the data structure.

    Contact toEntity(Map<String,Object> contact);

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    ContactDTO mapToDTO(Contact contact);
}
