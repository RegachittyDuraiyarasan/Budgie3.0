package com.hepl.budgie.mapper.userinfo;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.FamilyDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

import com.hepl.budgie.entity.userinfo.Family;
import org.mapstruct.ReportingPolicy;

import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedSourcePolicy = ReportingPolicy.IGNORE, unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface FamilyMapper {

    @Mapping(target = "emergencyContacts", expression = "java(java.util.List.of(mapEmergencyContacts(formRequest.getFormFields())))")

    Family toEntity(FormRequest formRequest);

    default EmergencyContacts mapEmergencyContacts(Map<String, Object> formFields){
        if (formFields == null){
            return null;
        }

        EmergencyContacts emergencyContacts = new EmergencyContacts();
        emergencyContacts.setContactName((String) formFields.get("contactName"));
        emergencyContacts.setContactNumber((String) formFields.get("contactNumber"));
        emergencyContacts.setGender((String) formFields.get("gender"));
        emergencyContacts.setRelationship((String) formFields.get("relationship"));
        emergencyContacts.setMaritalStatus((String) formFields.get("maritalStatus"));
        emergencyContacts.setBloodGroup((String) formFields.get("bloodGroup"));
        emergencyContacts.setEmergencyContact((boolean) formFields.get("emergencyContact"));
        emergencyContacts.setStatus(Status.ACTIVE.label);


        return emergencyContacts;
    }

    default String map(Object value) {
        return value == null ? null : value.toString();
    }

    FamilyDTO mapToDTO(Family family);
}
