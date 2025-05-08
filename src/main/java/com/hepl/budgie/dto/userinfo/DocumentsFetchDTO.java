package com.hepl.budgie.dto.userinfo;

import lombok.Data;

@Data
public class DocumentsFetchDTO {
    private Integer id;
    private String fileName;
    private String folderName;
}
