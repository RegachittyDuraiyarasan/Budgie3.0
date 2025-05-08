package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Contact {
    private String primaryContactNumber;
    private String secondaryContactNumber;
    private String personalEmailId;
    private String emergencyContactNumber;
    private Boolean isPermanentAddressDifferent;
    private PermanentAddressDetails permanentAddressDetails;
    private PresentAddressDetails presentAddressDetails;
}
