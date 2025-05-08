package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
@Component
public class BannerImageFileServiceImpl implements FilePathService {
    private final Path bannerLocation;
    public BannerImageFileServiceImpl(StorageProperties properties){
        this.bannerLocation= Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getBannerImage());
    }
    @Override
    public Path getDestinationPath() {
        return bannerLocation;
    }
}
