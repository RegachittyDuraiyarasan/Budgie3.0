package com.hepl.budgie.entity.documentinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDetails {
    private String folderName;
    private String fileName;
}
