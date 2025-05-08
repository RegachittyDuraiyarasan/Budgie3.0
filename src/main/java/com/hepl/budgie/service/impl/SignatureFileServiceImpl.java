package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SignatureFileServiceImpl implements FilePathService {
    private final Path signatureLocation;

    public SignatureFileServiceImpl(StorageProperties properties) {
        this.signatureLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getSignature());
    }

    @Override
    public Path getDestinationPath(){
        return signatureLocation;
    }
}
