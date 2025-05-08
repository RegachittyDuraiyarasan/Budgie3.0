package com.hepl.budgie.dto.form;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiFlow {

    private List<String> role;
    private String onFormInit;
    private String onSave;
    private String onUpdate;
    private String collectionName;

}
