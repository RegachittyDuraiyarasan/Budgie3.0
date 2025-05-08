package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class RelievingLetterFileServiceImpl implements FilePathService {
    private final Path relievingLetterLocation;

    public RelievingLetterFileServiceImpl(StorageProperties properties) {
        this.relievingLetterLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getRelievingLetter());
    }

    public Path getDestinationPath(){
        return relievingLetterLocation;
    }
}
