package com.hepl.budgie.entity.workflow;

import java.util.List;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class OrganizationDetails {
    private String OrganizationName;
    private List<String> subOrganization;
}
