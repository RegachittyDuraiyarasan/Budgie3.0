package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ExperienceFileServiceImpl implements FilePathService {
    private final Path experienceLocation;

    public ExperienceFileServiceImpl (StorageProperties properties){
        this.experienceLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getExperience());
    }

    public Path getDestinationPath(){
        return experienceLocation;
    }
}
