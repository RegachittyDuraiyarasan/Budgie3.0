package com.hepl.budgie.dto.userinfo;

import com.hepl.budgie.entity.userinfo.PermanentAddressDetails;
import com.hepl.budgie.entity.userinfo.PresentAddressDetails;
import lombok.Data;

@Data
public class ContactDTO {
    private String primaryContactNumber;
    private String secondaryContactNumber;
    private String personalEmailId;
    private String emergencyContactNumber;
    private Boolean isPermanentAddressDifferent;
    private PermanentAddressDetails permanentAddressDetails;
    private PresentAddressDetails presentAddressDetails;
}
