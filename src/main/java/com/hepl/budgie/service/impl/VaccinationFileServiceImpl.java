package com.hepl.budgie.service.impl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class VaccinationFileServiceImpl implements FilePathService {
    private final Path vaccinationLocation;

    public VaccinationFileServiceImpl(StorageProperties properties) {
        this.vaccinationLocation = Paths.get(properties.getLocation()).resolve(properties.getProfile()).resolve(properties.getOtherDocuments()).resolve(properties.getVaccination());
    }

    @Override
    public Path getDestinationPath(){
        return vaccinationLocation;
    }
}
