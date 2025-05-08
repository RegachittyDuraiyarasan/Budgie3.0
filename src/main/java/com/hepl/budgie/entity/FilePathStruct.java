package com.hepl.budgie.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilePathStruct {
    private String fileName;
    private String folderName;

}
