package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class IdCardGenerationServiceImpl implements FilePathService {
    private final Path idCardGeneration;

    public IdCardGenerationServiceImpl(StorageProperties properties) {
        this.idCardGeneration = Paths.get(properties.getLocation()).resolve(properties.getIdCard()).resolve(properties.getIdCardGenerator());
    }
    @Override
    public Path getDestinationPath(){
        return idCardGeneration;
    }
}
