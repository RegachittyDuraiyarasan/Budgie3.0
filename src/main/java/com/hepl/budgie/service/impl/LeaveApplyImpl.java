package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class LeaveApplyImpl implements FilePathService {
    private final Path leaveApplyLocation;

    public LeaveApplyImpl(StorageProperties properties) {
        this.leaveApplyLocation = Paths.get(properties.getLocation()).resolve(properties.getLeaveApply());
    }
    
    @Override
    public Path getDestinationPath() {
        return this.leaveApplyLocation;
    }
}
