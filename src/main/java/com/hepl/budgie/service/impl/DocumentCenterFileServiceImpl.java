package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
@Component
public class DocumentCenterFileServiceImpl implements FilePathService {
    private final Path documentCenterLocation;

    public DocumentCenterFileServiceImpl(StorageProperties properties){
        this.documentCenterLocation = Paths.get(properties.getLocation()).resolve(properties.getOtherDocuments());
    }
    @Override
    public Path getDestinationPath(){
        return documentCenterLocation;
    } 
}
