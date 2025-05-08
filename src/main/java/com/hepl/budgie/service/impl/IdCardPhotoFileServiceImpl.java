package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
@Component
public class IdCardPhotoFileServiceImpl implements FilePathService {
    private final Path idCardPhoto;

    public IdCardPhotoFileServiceImpl(StorageProperties properties) {
        this.idCardPhoto = Paths.get(properties.getLocation()).resolve(properties.getIdCardPhoto());
    }
    @Override
    public Path getDestinationPath(){
        return idCardPhoto;
    }
}
