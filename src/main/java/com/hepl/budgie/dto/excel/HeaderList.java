package com.hepl.budgie.dto.excel;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
@AllArgsConstructor
public class HeaderList {
    private String header;
    private boolean isMandatory;
    private String dataType;

}
