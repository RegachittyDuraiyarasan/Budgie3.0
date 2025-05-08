package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class AadhaarCardFileServiceImpl implements FilePathService {
    private final Path aadhaarCardLocation;

    public AadhaarCardFileServiceImpl(StorageProperties properties) {
        this.aadhaarCardLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getAadhaarCard());
    }

    @Override
    public Path getDestinationPath(){
        return aadhaarCardLocation;
    }
}
