package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
@Component
public class EmployeeDocumentFileServiceImp implements FilePathService {
 private final Path employeeDocumentLocation;

    public EmployeeDocumentFileServiceImp(StorageProperties properties){
        this.employeeDocumentLocation= Paths.get(properties.getLocation()).resolve(properties.getEmployeeDocument());
    }

    @Override
    public Path getDestinationPath() {
        return employeeDocumentLocation;
    }
}
