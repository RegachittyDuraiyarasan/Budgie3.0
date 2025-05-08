package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class OrganisationFileServiceImpl implements FilePathService {

    private final Path orgLocation;

    public OrganisationFileServiceImpl(StorageProperties properties) {
        this.orgLocation = Paths.get(properties.getLocation()).resolve(properties.getOrganization());
    }

    @Override
    public Path getDestinationPath() {
        return this.orgLocation;
    }

}
