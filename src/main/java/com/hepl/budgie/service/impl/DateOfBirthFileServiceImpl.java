package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class DateOfBirthFileServiceImpl implements FilePathService {
    private final Path dateOfBirthLocation;

    public DateOfBirthFileServiceImpl(StorageProperties properties) {
        this.dateOfBirthLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getDateOfBirthProof());
    }

    @Override
    public Path getDestinationPath(){
        return dateOfBirthLocation;
    }
}
