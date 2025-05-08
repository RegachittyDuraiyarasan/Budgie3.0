package com.hepl.budgie.config.events;

import java.io.IOException;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.stereotype.Component;

import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.service.FileService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrganizationMongoEventListener extends
        AbstractMongoEventListener<Organization> {

    private final MongoTemplate mongoTemplate;
    private final FileService fileService;

    @Override
    public void onBeforeSave(BeforeSaveEvent<Organization> event) {
        log.info("BeforeSaveEvent triggered for Organization: {}",
                event.getSource());

        Organization newOrg = event.getSource();

        if (newOrg.getId() != null) {
            Organization existingOrg = mongoTemplate.findById(newOrg.getId(),
                    Organization.class);

            if (existingOrg != null) {

                newOrg.setOrganizationDetail(existingOrg.getOrganizationDetail());

                if (existingOrg.getLogo() != null) {
                    String oldLogoPath = existingOrg.getLogo();
                    String newLogoPath = newOrg.getLogo();

                    if (newLogoPath != null && !newLogoPath.equals(oldLogoPath)) {
                        deleteLogoFile(oldLogoPath);
                    } else {
                        newOrg.setLogo(oldLogoPath);
                    }
                }
            }
        }

        log.info("Before save event: {}", newOrg);
    }

    private void deleteLogoFile(String logoPath) {
        try {
            fileService.deleteFile(logoPath, FileType.ORGANISATION);
        } catch (IOException e) {
            log.info("File IO error - {}", e.getLocalizedMessage());
        }
    }
}
