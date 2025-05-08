package com.hepl.budgie.controller.general;

import com.hepl.budgie.dto.countries.StateDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.countries.CountryDTO;
import com.hepl.budgie.entity.countriesdetails.City;
import com.hepl.budgie.entity.countriesdetails.State;
import com.hepl.budgie.service.general.CountriesAPIService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Countries API", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@RestController
@RequiredArgsConstructor
@RequestMapping("/country")
@Slf4j
public class CountriesAPIController {

    private final CountriesAPIService countryApiService;

    @GetMapping()
    public GenericResponse<List<CountryDTO>> getCountries() {
        log.info("Get countries");
        return GenericResponse.success(countryApiService.allCountries());
    }

    @GetMapping("/state")
    public GenericResponse<List<State>> getStatesByCountry(@RequestParam String country) {
        log.info("Get states by country {}", country);
        return GenericResponse.success(countryApiService.allStatesByCountry(country));
    }

    @GetMapping("/city")
    public GenericResponse<List<City>> getCitiesByStateAndCountry(@RequestParam String state,
            @RequestParam String country) {
        log.info("Get cities by state {} and country {}", country);
        return GenericResponse.success(countryApiService.allCitiesByStateAndCountry(state, country));
    }
    @GetMapping("/states")
    public GenericResponse<List<StateDTO>> getStates() {
        log.info("Get states");
        return GenericResponse.success(countryApiService.allStates());
    }

}
