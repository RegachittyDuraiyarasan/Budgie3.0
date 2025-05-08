package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
@Component
public class EventFileServiceImpl implements FilePathService {

    private final Path eventLocation;

    public EventFileServiceImpl (StorageProperties properties){
        this.eventLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getExperience());
    }

    public Path getDestinationPath(){
        return eventLocation;
    }
    
}
