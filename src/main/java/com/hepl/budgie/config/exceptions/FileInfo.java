package com.hepl.budgie.config.exceptions;

import com.hepl.budgie.entity.FileType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileInfo {

    private String fileName;
    private FileType fileType;

}
