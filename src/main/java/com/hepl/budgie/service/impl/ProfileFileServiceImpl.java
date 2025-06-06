package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class ProfileFileServiceImpl implements FilePathService {

    private final Path profileLocation;

    public ProfileFileServiceImpl(StorageProperties properties) {
        this.profileLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile());
    }

    @Override
    public Path getDestinationPath() {
        return this.profileLocation;
    }

}
