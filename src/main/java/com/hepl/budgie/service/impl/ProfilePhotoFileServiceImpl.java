package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
@Component
public class ProfilePhotoFileServiceImpl implements FilePathService {
    private final Path profileLocation;
    public ProfilePhotoFileServiceImpl(StorageProperties properties){
        this.profileLocation= Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getProfilePhoto());
    }

    @Override
    public Path getDestinationPath() {
        return profileLocation;
    }
}
