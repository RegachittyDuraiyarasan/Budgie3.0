package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class PassbookFileServiceImpl implements FilePathService {
    private final Path passbookLocation;

    public PassbookFileServiceImpl(StorageProperties properties) {
        this.passbookLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getBankPassbook());
    }

    @Override
    public Path getDestinationPath(){
        return passbookLocation;
    }
}
