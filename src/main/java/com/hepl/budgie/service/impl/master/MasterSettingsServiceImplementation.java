package com.hepl.budgie.service.impl.master;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.service.master.MasterSettingsService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MasterSettingsServiceImplementation implements MasterSettingsService {

    private final MasterSettingsRepository masterSettingsRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;


    @Override
    public List<MasterFormOptions> getSettingsByReferenceName(String referenceName) {
        Optional<MasterFormSettings> settings = masterSettingsRepository.fetchOptions(referenceName, jwtHelper.getOrganizationCode(),
                mongoTemplate);
        if (settings.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
        return settings.get().getOptions();
    }

    @Override
    public MasterFormSettings addOrUpdateSettings(FormRequest formRequest, String referenceName) {
        log.info("Add options for {}", formRequest.getFormName());
        log.info("Add options for reference name {}", referenceName);

        // Convert formFields (Map<String, Object>) into MasterFormOptions
        MasterFormOptions newOption = mapToMasterFormOptions(formRequest.getFormFields(), formRequest.getFormName());

        log.info("newOption{}", newOption);
        return masterSettingsRepository
                .addOptions(newOption, mongoTemplate, referenceName, jwtHelper)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
    }

    private MasterFormOptions mapToMasterFormOptions(Map<String, Object> formFields, String formName) {
        log.info("formType {}", formName);
        MasterFormOptions masterFormOptions = new MasterFormOptions();

        if (formFields.get("noticePeriod") != null) {
            masterFormOptions.setNoticePeriod((Integer) formFields.get("noticePeriod"));
        }

        if (formFields.get("mobileNumber") != null) {
            masterFormOptions.setMobileNumber((String) formFields.get("mobileNumber"));
        }

        if (formFields.get("email") != null) {
            masterFormOptions.setEmail((String) formFields.get("email"));
        }
        if (formFields.get("series") != null) {
            masterFormOptions.setSeries((String) formFields.get("series"));
        }
        if (formFields.get("probationStatus") != null) {
            masterFormOptions.setProbationStatus((String) formFields.get("probationStatus"));
        }
        if (formFields.get("eligibleForITDeclaration") != null) {
            masterFormOptions.setEligibleForITDeclaration((String) formFields.get("eligibleForITDeclaration"));
        }

        masterFormOptions.setName((String) formFields.get("name"));
        masterFormOptions.setValue((String) formFields.get("name"));
        masterFormOptions.setStatus(Status.ACTIVE.label);

        return masterFormOptions;
    }

    @Override
    public MasterFormSettings updateSettings(String value, String referenceName, FormRequest formRequest) {
        log.info("Update form options {}", referenceName);
        MasterFormOptions updatedOption = mapToMasterFormOptions(formRequest.getFormFields(),
                formRequest.getFormName());
        Optional<MasterFormSettings> updatedSettings = masterSettingsRepository.updateOption(value, referenceName,
                updatedOption, mongoTemplate, jwtHelper);

        return updatedSettings
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
    }

    // @Override
    // public void initializeMasterSettings(List<String> masters, Map<String, List<String>> predefinedMasters,
    //         String organisation) {
    //     log.info("Master settings initiation");
    //     masterSettingsRepository.upsertSettings(mongoTemplate, masters, predefinedMasters, organisation);
    // }

    @Override
    public void deleteOptions(String value, String referenceName) {
        UpdateResult result = masterSettingsRepository.deleteOptions(value, referenceName, mongoTemplate, jwtHelper);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void initIndexingSettingForOrganisation(String organisation) {
        log.info("Initializing master settings org {}", organisation);
        masterSettingsRepository.initMasterSettings(organisation, mongoTemplate);;
    }

    @Override
    public List<OptionsResponseDTO> fetchOptions(String org) {
        log.info("Fetching master setting options");
        return masterSettingsRepository.getOptions(mongoTemplate, org).getMappedResults();
    }

    @Override
    public MasterFormSettings updateStatusSettings(String value, String referenceName, String status) {
        Optional<MasterFormSettings> updatedSettings = masterSettingsRepository.updateStatus(value, referenceName,
                status, mongoTemplate, jwtHelper.getOrganizationCode());

        return updatedSettings
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));
    }

}
