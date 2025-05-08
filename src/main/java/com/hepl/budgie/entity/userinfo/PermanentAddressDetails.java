package com.hepl.budgie.entity.userinfo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermanentAddressDetails {
    private String permanentAddress;
    private String permanentState;
    private String permanentDistrict;
    private String permanentTown;
    private String permanentPinZipCode;
}
