package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.entity.userinfo.EmergencyContacts;
import lombok.Data;

import java.util.List;

@Data
public class FamilyDTO {
    private List<EmergencyContacts> emergencyContacts;
}
