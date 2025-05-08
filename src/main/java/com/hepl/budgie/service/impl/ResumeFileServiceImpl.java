package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ResumeFileServiceImpl implements FilePathService {

    private final Path resumeLocation;

    public ResumeFileServiceImpl(StorageProperties properties) {
        this.resumeLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getResume());
    }

    @Override
    public Path getDestinationPath() {
        return resumeLocation;
    }
}
