package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class BinFileServiceImpl implements FilePathService {

    private final Path binLocation;

    public BinFileServiceImpl(StorageProperties properties) {
        this.binLocation = Paths.get(properties.getLocation()).resolve(properties.getRecycle());
    }

    @Override
    public Path getDestinationPath() {
        return this.binLocation;
    }

}
