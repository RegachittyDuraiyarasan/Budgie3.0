package com.hepl.budgie.dto.iiy;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.ALWAYS)
public class IdeaCategoryMasterFetchDTO {
    private String id;
    private String ideaCategoryName;
    private int status;
    private int deleteStatus;
}
