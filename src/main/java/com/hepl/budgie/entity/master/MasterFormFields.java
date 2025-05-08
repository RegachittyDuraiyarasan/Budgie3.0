package com.hepl.budgie.entity.master;

import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.mapping.DBRef;

import com.hepl.budgie.dto.form.FieldAccessLevel;
import com.hepl.budgie.dto.form.ValidationDTO;
import com.hepl.budgie.entity.settings.MasterFormSettings;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterFormFields {

    private String fieldId;
    private String fieldName;
    private String type;
    private String initialValue;
    private Map<String, Integer> position;
    private String placeholder;
    private ValidationDTO validation;
    private String optionsReferenceLink;
    private List<MasterFormDependencies> dependencies;
    private Map<String, Object> attribute;
    private List<FieldAccessLevel> accessLevel;

    @DBRef
    private MasterFormSettings optionsReference;
    private MasterFormSettings optionsDefault;

}
