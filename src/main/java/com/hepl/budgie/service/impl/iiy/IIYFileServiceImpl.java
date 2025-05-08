package com.hepl.budgie.service.impl.iiy;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class IIYFileServiceImpl implements FilePathService {
    private final Path binLocation;

    public IIYFileServiceImpl(StorageProperties properties) {
        this.binLocation = Paths.get(properties.getLocation()).resolve(properties.getIiyCertificate());
    }

    @Override
    public Path getDestinationPath() {
        return this.binLocation;
    }

}
