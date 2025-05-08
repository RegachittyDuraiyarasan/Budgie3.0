package com.hepl.budgie.controller.organization;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.CreateDTO;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.UpdateDTO;
import com.hepl.budgie.dto.organization.OrganizationAddDTO;
import com.hepl.budgie.dto.organization.OrganizationMapAddDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.organization.OrganizationMap;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.organization.OrganizationService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@Tag(name = "Create and Manage organisation", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@Slf4j
@RequestMapping("/organization")
public class OrganizationController {

    private final OrganizationService organizationService;
    private final FileService fileService;
    private final Translator translator;
    private final MongoTemplate mongoTemplate;

    public OrganizationController(OrganizationService organizationService, Translator translator,
            FileService fileService, MongoTemplate mongoTemplate) {
        this.organizationService = organizationService;
        this.translator = translator;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
    }

    @GetMapping("/list-parOrgs")
    @Operation(summary = "Get data based on parentorganization")
    public GenericResponse<List<String>> getParentOrganizationList() {
        log.info("Get parentOrganization list");
        return GenericResponse.success(organizationService.getParentOrganizationList());
    }

    @GetMapping("/{parentId}/sub-orgs")
    @Operation(summary = "Get Data based on subOrganizationCode")
    public GenericResponse<List<Map<String, String>>> getSubOrganization(
            @RequestParam String parentOrganizationCode) {
        log.info("get Suborganization List");
        return GenericResponse.success(organizationService.getSubOrganization(parentOrganizationCode));
    }

    @GetMapping()
    @Operation(summary = "Get data based on organizationName")
    public GenericResponse<List<Organization>> fetch() {
        log.info("Get organisation list");
        return GenericResponse.success(organizationService.getOrganizationList());
    }

    @GetMapping("/options")
    @Operation(summary = "Get parent and child organisation")
    public GenericResponse<List<MasterFormOptions>> getOrganisationOptions(
            @RequestParam(defaultValue = "", required = false) String parentOrg, @RequestParam int isParent) {
        log.info("Get parent organisation");
        if (isParent == 0 && parentOrg.isEmpty()) {
            return GenericResponse.success(Collections.emptyList());
        }
        return GenericResponse.success(organizationService.getOrganizationOptions(parentOrg));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    @Operation(summary = "Add organization")
    public GenericResponse<String> add(@Validated(CreateDTO.class) @ModelAttribute OrganizationAddDTO request)
            throws IOException {
        log.info("Add org - {}", request);
        organizationService.add(request);

        return GenericResponse.success(translator.toLocale(AppMessages.ORG_ADDED));
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<String> update(@PathVariable String id,
            @Validated(UpdateDTO.class) @ModelAttribute OrganizationAddDTO request) throws IOException {
        log.info("Update organization {}", id);

        Optional<Organization> existingOrg = organizationService
                .findByOrganizationDetail(request.getOrganizationDetail(), mongoTemplate);
        if (existingOrg.isPresent() && !existingOrg.get().getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.ORG_ALREADY_EXITS);
        }

        if (request.getLogoFile() != null) {
            String path = fileService.uploadFile(request.getLogoFile(), FileType.ORGANISATION, "");
            request.setLogo(path);
        }
        if (request.getHeadSignature() != null) {
            String path = fileService.uploadFile(request.getHeadSignature(), FileType.ORGANISATION, "");
            request.setSignature(path);
        }
        organizationService.updateOrganization(id, request);
        return GenericResponse.success(translator.toLocale(AppMessages.DATA_UPDATED));
    }

    @DeleteMapping("/{id}")
    public GenericResponse<String> delete(@PathVariable String id) {
        log.info("Delete organisation {}", id);
        organizationService.deleteOrganization(id);
        return GenericResponse.success(translator.toLocale(AppMessages.ORG_DELETE));
    }

    @PostMapping("/map")
    @Operation(summary = "Add organization Map")
    public GenericResponse<String> addOrganizationMap(
            @Valid @RequestBody OrganizationMapAddDTO organizationMapRequest) {
        log.info("Add org - {}", organizationMapRequest);
        organizationService.addOrganizationMap(organizationMapRequest);
        return GenericResponse.success(translator.toLocale(AppMessages.ORG_MAP_ADD));
    }

    @PutMapping("/map/{id}")
    public GenericResponse<String> update(@PathVariable String id,
            @RequestBody OrganizationMapAddDTO updateRequest) {
        log.info("Organization id {}", id);

        organizationService.updateOrganizationMap(id, updateRequest);

        return GenericResponse.success(translator.toLocale(AppMessages.DATA_UPDATED));
    }

    @GetMapping("/map")
    @Operation(summary = "Get data based on organizationName")
    public GenericResponse<OrganizationMapAddDTO> fetch(@RequestParam String organizationName) {
        log.info("Get organisation Map by the organizationName");

        return GenericResponse.success(organizationService.getOrganizationMapByOrgName(organizationName));
    }

    @DeleteMapping("/map/{id}")
    public GenericResponse<String> deleteOrganizationMap(@PathVariable String id) {
        log.info("Delete organization map {}", id);

        organizationService.deleteOrganizationMap(id);
        return GenericResponse.success(translator.toLocale(AppMessages.ORG_DELETE));
    }

    @GetMapping("/file/{filename}")
    public ResponseEntity<byte[]> serveFile(@PathVariable String filename) throws IOException {
        log.info("Get filename .. {}", filename);
        Resource file = fileService.loadAsResource(filename, FileType.ORGANISATION);

        if (file == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.FILE_NOT_FOUND);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.parseMediaType(file.getURL().openConnection().getContentType()))
                .body(file.getContentAsByteArray());
    }

    @GetMapping("/organization-map")
    @Operation(summary = "Get data based on organizationName")
    public GenericResponse<List<OrganizationMap>> fetchOrganizationMap() {
        return GenericResponse.success(organizationService.getOrganizationMap());
    }

    @GetMapping("/organization-nonmap")
    @Operation(summary = "Get data based on organization non Mapped")
    public GenericResponse<List<Map<String, String>>> getNonMappedOrganizationCodes() {
        log.info("Fetching non-mapped organizations");
        List<Map<String, String>> nonMappedOrganizations = organizationService.getNonMappedOrganizationCodes();
        return GenericResponse.success(nonMappedOrganizations);
    }

    @PutMapping("/org-status-update/{id}")
    public GenericResponse<String> updateOrgStatus(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        organizationService.updateOrganizationStatus(id, request.get("status"));
        return GenericResponse.success(translator.toLocale(AppMessages.DATA_UPDATED));
    }

    @PutMapping("/orgMap-status-update/{id}")
    public GenericResponse<String> updateOrgMapStatus(@PathVariable String id,
            @RequestBody Map<String, String> request) {
        organizationService.updateOrganizationMapStatus(id, request.get("status"));
        return GenericResponse.success(translator.toLocale(AppMessages.DATA_UPDATED));
    }
}
