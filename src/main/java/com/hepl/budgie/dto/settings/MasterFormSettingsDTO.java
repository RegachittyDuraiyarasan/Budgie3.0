package com.hepl.budgie.dto.settings;

import com.hepl.budgie.entity.master.MasterFormOptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MasterFormSettingsDTO {
    private String referenceName;
    private MasterFormOptions options;
}
