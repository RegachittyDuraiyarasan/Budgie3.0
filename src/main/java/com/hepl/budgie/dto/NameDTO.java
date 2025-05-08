package com.hepl.budgie.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class NameDTO {

    @NotBlank(message = "Name Cannot Be Blank")
    @NotNull(message = "Name Cannot Be Null")
	private String name;
}
