package com.hepl.budgie.entity.companypolicy;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileDetails {
     private String folderName;
     private String fileName;
}
