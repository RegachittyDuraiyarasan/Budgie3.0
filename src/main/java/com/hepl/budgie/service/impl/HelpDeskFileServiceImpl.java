package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class HelpDeskFileServiceImpl implements FilePathService {
    private final Path helpdeskLocation;

    public HelpDeskFileServiceImpl(StorageProperties properties){
        this.helpdeskLocation= Paths.get(properties.getLocation()).resolve(properties.getHelpdesk());
    }

    @Override
    public Path getDestinationPath() {
        return helpdeskLocation;
    }
}
