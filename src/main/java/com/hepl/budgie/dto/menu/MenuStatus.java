package com.hepl.budgie.dto.menu;

import com.hepl.budgie.config.annotation.ValueOfEnum;
import com.hepl.budgie.entity.Status;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuStatus {

    @NotBlank(message = "{validation.error.blank}")
    private String name;
    @ValueOfEnum(enumClass = Status.class, args = "Active, Inactive, Deleted", message = "{validation.error.enumValue}")
    private String status;

}
