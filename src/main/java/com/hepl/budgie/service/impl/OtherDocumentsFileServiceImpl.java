package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class OtherDocumentsFileServiceImpl implements FilePathService {

    private final Path otherDocumentLocation;

    public OtherDocumentsFileServiceImpl(StorageProperties properties) {
        this.otherDocumentLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments());
    }

    @Override
    public Path getDestinationPath(){
        return otherDocumentLocation;
    }
}
