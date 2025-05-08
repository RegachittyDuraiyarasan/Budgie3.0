package com.hepl.budgie.entity.master;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Trigger {
    private String triggerType;
    private String triggerId;
}
