package com.hepl.budgie.service.impl;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Component;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;

@Component
public class HolidayFileServiceImpl implements FilePathService{
     private final Path holiday;

    public HolidayFileServiceImpl(StorageProperties properties){
        this.holiday = Paths.get(properties.getLocation()).resolve(properties.getHoliday());
    }

    @Override
    public Path getDestinationPath(){
        return holiday;
    }
}
