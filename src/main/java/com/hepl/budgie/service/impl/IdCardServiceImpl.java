package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class IdCardServiceImpl implements FilePathService {
    private final Path idCardLocation;

    public IdCardServiceImpl(StorageProperties properties) {
        this.idCardLocation = Paths.get(properties.getLocation()).resolve(properties.getIdCard());
    }

    @Override
    public Path getDestinationPath() {
        return idCardLocation;
    }
}
