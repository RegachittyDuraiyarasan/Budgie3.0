package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class EducationFileServiceImpl implements FilePathService {
    private final Path educationLocation;

    public EducationFileServiceImpl(StorageProperties properties){
        this.educationLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getEducation());
    }
    @Override
    public Path getDestinationPath(){
        return educationLocation;
    }
}
