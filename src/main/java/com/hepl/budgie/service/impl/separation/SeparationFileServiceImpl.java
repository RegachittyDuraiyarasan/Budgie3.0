package com.hepl.budgie.service.impl.separation;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

    @Component
public class SeparationFileServiceImpl implements FilePathService {

    private final Path binLocation;

    public SeparationFileServiceImpl(StorageProperties properties) {
        this.binLocation = Paths.get(properties.getLocation()).resolve(properties.getSeparation());
    }

    @Override
    public Path getDestinationPath() {
        return this.binLocation;
    }

}
