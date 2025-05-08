package com.hepl.budgie.service.impl.organization;

import com.hepl.budgie.config.exceptions.CustomDuplicatekeyException;
import com.hepl.budgie.config.exceptions.CustomResponseStatusException;
import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.dto.organization.OrganizationMapAddDTO;
import com.hepl.budgie.dto.organization.OrganizationRef;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.OrganizationMap;
import com.hepl.budgie.facade.DataInitFacade;
import com.hepl.budgie.mapper.organization.OrganizationMapper;
import com.hepl.budgie.repository.general.CountriesRepository;
import com.hepl.budgie.repository.organization.OrganizationMapRepository;
import com.hepl.budgie.repository.organization.OrganizationRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.organization.OrganizationService;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.Collections;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrganizationServiceImplementation implements OrganizationService {

    private static final String ORG_SEQUENCE = "ORG00000";
    private static final String GID_SEQUENCE = "GID00000";
    private final OrganizationRepository organizationRepository;
    private final OrganizationMapper organizationMapper;
    private final MongoTemplate mongoTemplate;
    private final DataInitFacade dataInitFacade;
    private final OrganizationMapRepository organizationMapRepository;
    private final FileService fileService;
    private final CountriesRepository countriesRepository;

    @Override
    public List<String> getParentOrganizationList() {
        log.info("Fetching parent organization list");

        Query query = new Query(Criteria.where("parentOrganization").exists(true));
        List<OrganizationMap> parentOrganizations = mongoTemplate.find(query, OrganizationMap.class);

        if (parentOrganizations.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No parent organizations found");
        }

        return parentOrganizations.stream()
                .map(orgMap -> orgMap.getParentOrganization().getOrganizationCode()).toList();
    }

    // @Override
    // public List<String> getSubOrganization(String parentOrganizationCode) {
    //     log.info("Fetching sub-organization list for parent organization: {}", parentOrganizationCode);

    //     Query query = new Query(Criteria.where("parentOrganization.organizationCode").is(parentOrganizationCode));
    //     List<OrganizationMap> organizationMaps = mongoTemplate.find(query, OrganizationMap.class);

    //     if (organizationMaps.isEmpty()) {
    //         throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.NOT_FOUND_SUB_ORG);
    //     }

    //     return organizationMaps.stream()
    //             .flatMap(orgMap -> orgMap.getOrganizationMapping().stream())
    //             .map(OrganizationRef::getOrganizationCode).toList();
    // }
    
    @Override
    public List<Map<String, String>> getSubOrganization(String parentOrganizationCode) {
        log.info("Fetching sub-organization list for parent organization: {}", parentOrganizationCode);
    
        // Query to find the sub-organizations for the given parent organization code
        Query query = new Query(Criteria.where("parentOrganization.organizationCode").is(parentOrganizationCode));
        List<OrganizationMap> organizationMaps = mongoTemplate.find(query, OrganizationMap.class);
    
        if (organizationMaps.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.NOT_FOUND_SUB_ORG);
        }
    
        // Extract sub-organization codes and details
        List<Map<String, String>> subOrgDetails = organizationMaps.stream()
                .flatMap(orgMap -> orgMap.getOrganizationMapping().stream())
                .map(orgRef -> {
                    Map<String, String> orgDetails = new HashMap<>();
                    orgDetails.put("organizationCode", orgRef.getOrganizationCode());
                    orgDetails.put("organizationDetail", orgRef.getOrganizationDetail());
                    return orgDetails;
                })
                .collect(Collectors.toList());
    
        return subOrgDetails;
    }

    @Override
    public Optional<Organization> findByOrganizationDetail(String organizationDetail, MongoTemplate mongoTemplate) {
        return organizationRepository.findByOrganizationDetail(organizationDetail, mongoTemplate);
    }

    @Override
    public void add(OrganizationAddDTO request) throws IOException {
        log.info("Adding organisation");

        if (organizationRepository.findByOrganizationDetail(request.getOrganizationDetail(), mongoTemplate)
                .isPresent()) {
            throw new CustomResponseStatusException(AppMessages.ORG_ALREADY_EXITS, HttpStatus.CONFLICT,null);
        }

        if (request.getLogoFile() != null) {
            String path = fileService.uploadFile(request.getLogoFile(),
                    FileType.ORGANISATION, "");
            request.setLogo(path);
        }
        if (request.getHeadSignature() != null) {
            String signaturePath = fileService.uploadFile(request.getHeadSignature(),
                    FileType.ORGANISATION, "");
            request.setSignature(signaturePath);
        }

        Country country = countriesRepository.findByName(request.getCountry()).get();

        Organization organization = organizationMapper.toEntity(request,
                Status.ACTIVE);
        organization.setIso3(country.getIso3());
        organization.setOrganizationCode(organizationRepository.findTopByOrderByIdDesc()
                .map(e -> AppUtils.generateUniqueId(e.getOrganizationCode()))
                .orElse(AppUtils.generateUniqueId(ORG_SEQUENCE)));

        organizationRepository.save(organization);
        dataInitFacade.initOrganisationData(organization.getOrganizationCode());
    }

    @Override
    public void updateOrganization(String id, OrganizationAddDTO request) {
        log.info("Updating organisation");

        Optional<Organization> updatedSettings = organizationRepository.updateOrganization(id,
                request, mongoTemplate);
        if (updatedSettings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

    }

    @Override
    public void deleteOrganization(String id) {
        Organization organization = organizationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        String organizationCode = organization.getOrganizationCode();

        Set<String> mappedOrganizationCodes = organizationMapRepository.findAll().stream()
                .flatMap(orgMap -> orgMap.getOrganizationMapping().stream())
                .map(OrganizationRef::getOrganizationCode)
                .collect(Collectors.toSet());
        Set<String> mappedOrganizationCodeParentOrganization = organizationMapRepository.findAll().stream()
                .map(OrganizationMap::getParentOrganization)
                .filter(Objects::nonNull)
                .map(OrganizationRef::getOrganizationCode)
                .collect(Collectors.toSet());

        if (mappedOrganizationCodes.contains(organizationCode)
                || mappedOrganizationCodeParentOrganization.contains(organizationCode)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.ORGANIZATION_ALREADY_MAPPED);
        }

        UpdateResult result = organizationRepository.deleteOrganization(id, mongoTemplate);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        log.info("Organization with ID {} deleted successfully", id);
    }

    @Override
    public List<Organization> getOrganizationList() {
        Query query = new Query(Criteria.where("status").ne(Status.DELETED.label));
        return mongoTemplate.find(query,
                Organization.class);
    }

    @Override
    public void updateOrganizationMap(String id, OrganizationMapAddDTO updateRequest) {
        validateOrganizationCodes(updateRequest.getOrganizationDetail(),
                new HashSet<>(updateRequest.getOrganizationMapping())); // Convert List to Set
        validateIfOrgMappingExists(updateRequest.getOrganizationDetail(),
                new HashSet<>(updateRequest.getOrganizationMapping()), id); // Pass null since it's an add operation

        Optional<OrganizationMap> updatedOrganizationMap = organizationMapRepository.updateOrganizationMap(id,
                updateRequest, mongoTemplate);

        if (updatedOrganizationMap.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    public void addOrganizationMap(OrganizationMapAddDTO organizationMapRequest) {
        log.info("Adding organization map");

        // Validate organization codes
        validateOrganizationCodes(organizationMapRequest.getOrganizationDetail(),
                new HashSet<>(organizationMapRequest.getOrganizationMapping())); // Convert List to Set

        // Validate if organizationDetail or organizationMapping is already mapped
        validateIfOrgMappingExists(organizationMapRequest.getOrganizationDetail(),
                new HashSet<>(organizationMapRequest.getOrganizationMapping()), null); // Pass null since it's an add
                                                                                       // operation

        List<OrganizationRef> parentOrganization = organizationRepository.getOrganisationReference(mongoTemplate,
                List.of(organizationMapRequest.getOrganizationDetail())).getMappedResults();

        List<OrganizationRef> childOrganization = organizationRepository
                .getOrganisationReference(mongoTemplate, organizationMapRequest.getOrganizationMapping())
                .getMappedResults();

        if (parentOrganization.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PARENT_ORG_NOT_EXIST);
        }

        if (childOrganization.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.MULTI_CHILD_ORG_NOT_EXIST);
        }

        String groupId = organizationMapRepository.findTopByOrderByIdDesc()
                .map(e -> AppUtils.generateUniqueId(e.getGroupId()))
                .orElse(AppUtils.generateUniqueId(GID_SEQUENCE));

        OrganizationMap organizationMap = OrganizationMap.builder()
                .groupId(groupId) // Set auto-generated groupId
                .parentOrganization(parentOrganization.get(0))
                .organizationMapping(childOrganization)
                .status(Status.ACTIVE.label)
                .build();

        organizationMapRepository.save(organizationMap);
        organizationRepository.updateGroupIdForOrgs(organizationMapRequest.getOrganizationMapping(), groupId,
                mongoTemplate);
        dataInitFacade.groupOrganizationData(groupId);
    }

    /**
     * Validates if the provided organization codes exist in the Organization
     * collection.
     */
    private void validateOrganizationCodes(String organizationDetail, Set<String> organizationMapping) {
        Set<String> organizationCodesToValidate = new HashSet<>(organizationMapping);
        organizationCodesToValidate.add(organizationDetail);

        // Fetch all existing organization codes
        Set<String> existingOrganizationCodes = organizationRepository.findAll().stream()
                .map(Organization::getOrganizationCode)
                .collect(Collectors.toSet());

        // Identify invalid codes
        Set<String> invalidCodes = organizationCodesToValidate.stream()
                .filter(code -> !existingOrganizationCodes.contains(code))
                .collect(Collectors.toSet());

        if (!invalidCodes.isEmpty()) {
            throw new CustomResponseStatusException(
                    AppMessages.ORG_NOT_FOUND,
                    HttpStatus.BAD_REQUEST,
                    invalidCodes.toArray(new String[0]) // Convert Set<String> to String[]
            );
        }
    }

    /**
     * Checks if the organizationDetail or organizationMapping is already mapped in
     * another OrganizationMap.
     */
    private void validateIfOrgMappingExists(String organizationDetail, Set<String> organizationMapping,
            String excludeId) {
        Set<String> mappedOrganizationCodes = organizationMapRepository.findAll().stream()
                .filter(orgMap -> excludeId == null || !orgMap.getId().equals(excludeId)) // Exclude the current ID if
                                                                                          // updating
                .flatMap(orgMap -> Stream.concat(
                        Stream.of(orgMap.getParentOrganization().getOrganizationCode()), // Parent organization
                        orgMap.getOrganizationMapping().stream().map(OrganizationRef::getOrganizationCode) // Child
                                                                                                           // mappings
                ))
                .collect(Collectors.toSet());

        Set<String> duplicateCodes = organizationMapping.stream()
                .filter(mappedOrganizationCodes::contains)
                .collect(Collectors.toSet());

        if (mappedOrganizationCodes.contains(organizationDetail)) {
            duplicateCodes.add(organizationDetail);
        }

        if (!duplicateCodes.isEmpty()) {
            throw new CustomResponseStatusException(
                    AppMessages.ORG_ALREADY_MAPPED,
                    HttpStatus.BAD_REQUEST,
                    duplicateCodes.toArray(new String[0]));
        }
    }

    @Override
    public OrganizationMapAddDTO getOrganizationMapByOrgName(String organizationName) {
        return organizationMapRepository
                .findByParentOrganization(organizationName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
    }

    @Override
    public void deleteOrganizationMap(String id) {
        UpdateResult result = organizationRepository.deleteOrganizationMap(id, mongoTemplate);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public List<MasterFormOptions> getOrganizationOptions(String parentOrg) {
        if (parentOrg.isEmpty()) {
            log.info("Get parent organizations");
            return organizationMapRepository.getParentOrganization(mongoTemplate).getMappedResults();
        } else {
            log.info("Get child org by parent organizations, {}", parentOrg);
            return organizationMapRepository.getChildOrganization(parentOrg, mongoTemplate).getMappedResults();
        }
    }

    @Override
    public List<OrganizationMap> getOrganizationMap() {
        Query query = new Query(Criteria.where("status").ne("Deleted"));
        List<OrganizationMap> organizationmap = mongoTemplate.find(query, OrganizationMap.class);
        if (organizationmap.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
        return organizationmap;
    }

    @Override
    public List<Map<String, String>> getNonMappedOrganizationCodes() {
        log.info("Fetching non-mapped organizations");

        List<Organization> allOrganizations = organizationRepository.findAll();
        log.info("Total organizations found: {}", allOrganizations.size());

        List<OrganizationMap> mappedOrganizations = organizationMapRepository.findAll();
        log.info("Total mapped organizations found: {}", mappedOrganizations.size());

        Set<String> mappedOrgCodes = new HashSet<>();

        mappedOrganizations.forEach(orgMap -> {

            if (orgMap.getParentOrganization() != null) {
                mappedOrgCodes.add(orgMap.getParentOrganization().getOrganizationCode());
            }

            if (orgMap.getOrganizationMapping() != null && !orgMap.getOrganizationMapping().isEmpty()) {
                mappedOrgCodes.addAll(
                        orgMap.getOrganizationMapping().stream()
                                .map(OrganizationRef::getOrganizationCode)
                                .collect(Collectors.toSet()));
            }

        });

        List<Map<String, String>> nonMappedOrganizations = allOrganizations.stream()
                .filter(org -> !mappedOrgCodes.contains(org.getOrganizationCode()))
                .map(org -> {
                    Map<String, String> orgData = new HashMap<>();
                    orgData.put("organizationCode", org.getOrganizationCode());
                    orgData.put("organizationDetail", org.getOrganizationDetail());
                    return orgData;
                })
                .toList();

        log.info("Non-mapped organizations found: {}", nonMappedOrganizations.size());
        return nonMappedOrganizations;
    }

    @Override
    public void updateOrganizationStatus(String id, String status) {
        Optional<Organization> updatedOrganizationStatus = organizationRepository.updateOrganizationStatus(id,
                status, mongoTemplate);
        if (updatedOrganizationStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void updateOrganizationMapStatus(String id, String status) {
        Optional<OrganizationMap> updatedOrganizationMapStatus = organizationMapRepository.updateOrganizationMapStatus(
                id,
                status, mongoTemplate);
        if (updatedOrganizationMapStatus.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

}
