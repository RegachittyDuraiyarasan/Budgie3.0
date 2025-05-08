package com.hepl.budgie.entity.countriesdetails;

import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "countries")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Country {

    private String id;
    private String name;
    private String iso3;
    private String currency;
    private String region;
    private String subregion;
    private String phonecode;
    @JsonProperty("numeric_code")
    private String numericCode;
    private List<State> states;

}
