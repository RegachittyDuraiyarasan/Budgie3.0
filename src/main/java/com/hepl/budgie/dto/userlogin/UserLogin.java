package com.hepl.budgie.dto.userlogin;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserLogin {
    @NotBlank(message = "{validation.login.error.empIdCannotBeBlank}")
    private String empId;
    @NotBlank(message = "{validation.login.error.passwordCannotBeBlank}")
    private String password;
}
