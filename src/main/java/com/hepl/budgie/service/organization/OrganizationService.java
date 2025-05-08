package com.hepl.budgie.service.organization;

import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.dto.organization.OrganizationMapAddDTO;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.OrganizationMap;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;

import org.springframework.data.mongodb.core.MongoTemplate;

public interface OrganizationService {
    void add(OrganizationAddDTO request) throws IOException;

    void updateOrganization(String id, OrganizationAddDTO request);

    void deleteOrganization(String id);

    List<Organization> getOrganizationList();

    List<String> getParentOrganizationList();

    List<Map<String, String>> getSubOrganization(String parentOrganizationCode) ;

    void addOrganizationMap(@Valid OrganizationMapAddDTO organizationMapRequest);

    void updateOrganizationMap(String id, OrganizationMapAddDTO updateRequest);

    OrganizationMapAddDTO getOrganizationMapByOrgName(String organizationName);

    void deleteOrganizationMap(String id);

    List<MasterFormOptions> getOrganizationOptions(String parentOrg);

    // Object getOrganizationM
    List<OrganizationMap> getOrganizationMap();

    // List<Organization> getNonMappedOrganizationCodes();
    List<Map<String, String>> getNonMappedOrganizationCodes();

    void updateOrganizationStatus(String id, String status);

    void updateOrganizationMapStatus(String id, String status);

    Optional<Organization> findByOrganizationDetail(String organizationDetail, MongoTemplate mongoTemplate);

}
