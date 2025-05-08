package com.hepl.budgie.dto.userinfo;

import lombok.Data;

@Data
public class PasswordDTO {
    private String newPassword;
    private String confirmPassword;
}
