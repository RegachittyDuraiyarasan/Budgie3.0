package com.hepl.budgie.service.impl.general;

import java.util.List;

import com.hepl.budgie.dto.countries.StateDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.dto.countries.CountryDTO;
import com.hepl.budgie.entity.countriesdetails.City;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.countriesdetails.State;
import com.hepl.budgie.repository.general.CountriesRepository;
import com.hepl.budgie.service.general.CountriesAPIService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CountriesAPIServiceImpl implements CountriesAPIService {

    private final CountriesRepository countriesRepository;
    private final MongoTemplate mongoTemplate;

    public CountriesAPIServiceImpl(CountriesRepository countriesRepository, MongoTemplate mongoTemplate) {
        this.countriesRepository = countriesRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveCountries(List<Country> countryDetails) {
        log.info("Save countries");
        countriesRepository.saveAll(countryDetails);
    }

    @Override
    public List<CountryDTO> allCountries() {
        log.info("Get all countries");
        return countriesRepository.getCountries(mongoTemplate);
    }

    @Override
    public List<State> allStatesByCountry(String country) {
        log.info("Get states by country {}", country);
        return countriesRepository.getStatesByCountryName(country, mongoTemplate);
    }

    @Override
    public List<City> allCitiesByStateAndCountry(String state, String country) {
        log.info("Get cities by country {} and state {}", country, state);
        return countriesRepository.getCitiesByStateAndCountry(country, state, mongoTemplate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND))
                .getCities();
    }
    @Override
    public List<StateDTO> allStates() {
        log.info("Get all states");
        return countriesRepository.getStates(mongoTemplate);
    }

}
