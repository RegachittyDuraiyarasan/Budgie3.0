package com.hepl.budgie.service.impl.fileServiceImpl;

import com.hepl.budgie.config.properties.StorageProperties;
import com.hepl.budgie.service.FilePathService;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class ReimbursementFileService implements FilePathService {

    private final Path reimbursementPath;

    public ReimbursementFileService(StorageProperties properties) {
        this.reimbursementPath = Paths.get(properties.getLocation()).resolve(properties.getReimbursement());
    }

    @Override
    public Path getDestinationPath() {
        return reimbursementPath;
    }
}
