package com.hepl.budgie.dto.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentDetailDTO {
    private String fileName;
    private List<String> filePath;
    private String folderName;
    private List<String> title;
    private String status;
}
