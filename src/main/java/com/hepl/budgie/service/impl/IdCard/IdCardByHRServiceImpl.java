package com.hepl.budgie.service.impl.IdCard;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
@Component
public class IdCardByHRServiceImpl implements FilePathService{
    
    private Path idCardLocation;

    public IdCardByHRServiceImpl(StorageProperties properties) {
        this.idCardLocation = Paths.get(properties.getLocation()).resolve(properties.getIdCardPhotoHr());
    }

    @Override
    public Path getDestinationPath() {
        return this.idCardLocation;
    }
}
