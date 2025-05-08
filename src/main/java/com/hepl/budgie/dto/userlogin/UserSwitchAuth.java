package com.hepl.budgie.dto.userlogin;

import com.hepl.budgie.config.annotation.ValueOfEnum;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserSwitchAuth {

    @ValueOfEnum(enumClass = AuthSwitch.class, message = "{validation.error.enumValue}", args = "Role, Organization")
    private String type;
    @NotBlank(message = "{validation.error.blank}")
    private String value;

}
