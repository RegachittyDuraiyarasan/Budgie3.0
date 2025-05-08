package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class CompanyPolicyServiceImplementation implements FilePathService{
    private final Path companyPolicyLocation;

    public CompanyPolicyServiceImplementation(StorageProperties properties){
        this.companyPolicyLocation= Paths.get(properties.getLocation()).resolve(properties.getCompanyPolicy());
    }

    @Override
    public Path getDestinationPath(){
        return companyPolicyLocation;
    }
}
