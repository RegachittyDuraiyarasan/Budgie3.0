package com.hepl.budgie.entity.menu;

import com.hepl.budgie.config.annotation.ValueOfEnum;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Condition {

    @ValueOfEnum(args = "", enumClass = ConditionType.class)
    private String key;
    private Object value;

}
