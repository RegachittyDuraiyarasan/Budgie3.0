package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PassportFileServiceImpl implements FilePathService {

    private final Path passportLocation;

    public PassportFileServiceImpl(StorageProperties properties) {
        this.passportLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getPassportPhoto());
    }

    @Override
    public Path getDestinationPath() {
        return passportLocation;
    }
}
