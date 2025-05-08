package com.hepl.budgie.service.general;

import java.util.List;

import com.hepl.budgie.dto.countries.CountryDTO;
import com.hepl.budgie.dto.countries.StateDTO;
import com.hepl.budgie.entity.countriesdetails.City;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.countriesdetails.State;

public interface CountriesAPIService {

    void saveCountries(List<Country> countryDetails);

    List<CountryDTO> allCountries();

    List<State> allStatesByCountry(String country);

    List<City> allCitiesByStateAndCountry(String state, String country);

    List<StateDTO> allStates();
}
