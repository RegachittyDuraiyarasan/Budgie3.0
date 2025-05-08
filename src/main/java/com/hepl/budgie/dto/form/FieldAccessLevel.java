package com.hepl.budgie.dto.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FieldAccessLevel {

    private List<String> role;
    private boolean disabled;
    private boolean required;
    private boolean show;
    private String type;

}
