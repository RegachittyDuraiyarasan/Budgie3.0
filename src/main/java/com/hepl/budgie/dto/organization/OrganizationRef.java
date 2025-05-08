package com.hepl.budgie.dto.organization;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrganizationRef {

    private String organizationDetail;
    private String organizationCode;
    private String country;
    private String iso3;
    private String groupId;
    private String logo;
//    @JsonIgnore
    private List<String> roleDetails;

}
