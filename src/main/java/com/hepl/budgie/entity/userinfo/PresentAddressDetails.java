package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PresentAddressDetails {
    private String presentAddress;
    private String presentState;
    private String presentDistrict;
    private String presentTown;
    private String presentPinZipCode;
}
