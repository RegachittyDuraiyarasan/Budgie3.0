package com.hepl.budgie.service.impl.master;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.repository.master.ModuleMasterSettingsRepository;
import com.hepl.budgie.service.master.ModuleMasterSettingsService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
 public class ModuleMasterSettingsImplementation implements ModuleMasterSettingsService {

    private final ModuleMasterSettingsRepository moduleMasterRepository;

    private final MongoTemplate mongoTemplate;

    private final JWTHelper jwtHelper;

    @Override
    public void addModuleMaster(FormRequest formRequest, String referenceName, String org) {

        Map<String, Object> option = formRequest.getFormFields();
        log.info("option {}",option);
        boolean addedModuleMaster = moduleMasterRepository
                .addOptions(option, mongoTemplate, referenceName, org);
        log.info("addedModuleMaster {}",addedModuleMaster);

    }

    @Override
    public List<ModuleMaster> getOptionsByReferenceName(String referenceName, String org) {
        return moduleMasterRepository.fetchOptions(mongoTemplate, referenceName, org);
    }

    @Override
    public ModuleMaster updateModuleMaster(FormRequest formRequest, String referenceName, String org, String moduleId){
        Map<String, Object> updatedOption = formRequest.getFormFields();
        boolean updatedModuleMaster = moduleMasterRepository
                .updateOptions(updatedOption, mongoTemplate, referenceName, moduleId, org);
        log.info("updatedModuleMaster {}",updatedModuleMaster);
        return null;
    }

    @Override
    public void deleteOptions(String moduleId, String referenceName) {
        UpdateResult result = moduleMasterRepository.deleteOptions(moduleId , referenceName, mongoTemplate, jwtHelper.getOrganizationCode());
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void initIndexingModuleMasterSettings( String organisation){
        log.info("Initialize indexing for org {}", organisation);
        moduleMasterRepository.initMastermoduleSettings(organisation, mongoTemplate);
    }
}
