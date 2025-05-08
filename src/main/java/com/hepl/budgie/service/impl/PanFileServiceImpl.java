package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PanFileServiceImpl implements FilePathService {
    private final Path panLocation;

    public PanFileServiceImpl(StorageProperties properties) {
        this.panLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getPan());
    }

    @Override
    public Path getDestinationPath(){
        return panLocation;
    }
}
