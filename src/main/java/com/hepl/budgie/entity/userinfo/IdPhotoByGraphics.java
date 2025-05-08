package com.hepl.budgie.entity.userinfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IdPhotoByGraphics {
    private String fileName;
    private String folderName;
    private String photoStatusByGraphics;
    private String createdBy;
    private ZonedDateTime submittedOn;
}
