package com.hepl.budgie.dto.countries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CountryDTO {

    private String name;
    private String iso3;
    private String currency;

}
