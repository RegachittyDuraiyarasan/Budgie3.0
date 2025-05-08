package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class CourseCategoryMasterFetchDTO {
    private String id;
    private String categoryName;
    private int status;
    private int deleteStatus;

}
