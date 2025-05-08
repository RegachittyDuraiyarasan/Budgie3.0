package com.hepl.budgie.entity.userinfo;

import lombok.Data;

@Data
public class EmergencyContacts {
    private String familyId;
    private String contactName;
    private String contactNumber;
    private String gender;
    private String relationship;
    private String maritalStatus;
    private String bloodGroup;
    private boolean emergencyContact;
    private String status;
}
