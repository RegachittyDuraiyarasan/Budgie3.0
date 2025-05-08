package com.hepl.budgie.entity.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.hepl.budgie.dto.organization.OrganizationRef;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document("organisation_map")
@Builder
public class OrganizationMap {
    @Id
    private String id;
    private String groupId;
    private OrganizationRef parentOrganization;
    private List<OrganizationRef> organizationMapping;
    private String status;

    public String getParentOrganizationId() {
        return parentOrganization != null ? parentOrganization.getOrganizationCode() : null;
    }

}
