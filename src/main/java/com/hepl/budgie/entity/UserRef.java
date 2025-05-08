package com.hepl.budgie.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserRef {
    private String empId;
    private String organizationCode;
    private String organizationGroupCode;
    private String activeRole;
}
