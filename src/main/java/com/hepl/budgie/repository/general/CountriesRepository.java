package com.hepl.budgie.repository.general;

import java.util.List;
import java.util.Optional;

import com.hepl.budgie.dto.countries.StateDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.countries.CountryDTO;
import com.hepl.budgie.entity.countriesdetails.Country;
import com.hepl.budgie.entity.countriesdetails.State;

@Repository
public interface CountriesRepository extends MongoRepository<Country, String> {

        public static final String COLLECTION_NAME = "countries";

        Optional<Country> findByName(String name);

        default List<CountryDTO> getCountries(MongoTemplate mongoTemplate) {

                ProjectionOperation projection = Aggregation.project("name", "iso3", "currency");
                Aggregation aggregation = Aggregation.newAggregation(projection);

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                CountryDTO.class).getMappedResults();
        }

        default List<State> getStatesByCountryName(String country, MongoTemplate mongoTemplate) {

                MatchOperation matchOperation = Aggregation.match(Criteria.where("name").is(country));
                UnwindOperation unwindOperation = Aggregation.unwind("states");
                ProjectionOperation projection = Aggregation.project().and("$states.name").as("name")
                                .and("$states.stateCode")
                                .as("stateCode");
                Aggregation aggregation = Aggregation.newAggregation(matchOperation, unwindOperation, projection);

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                State.class).getMappedResults();
        }

        default Optional<State> getCitiesByStateAndCountry(String country, String state, MongoTemplate mongoTemplate) {

                MatchOperation matchOperation = Aggregation.match(Criteria.where("name").is(country));

                ComparisonOperators.Eq equal = ComparisonOperators.Eq.valueOf("$$state.name").equalToValue(state);
                ArrayOperators.Filter filter = ArrayOperators.Filter.filter("states").as("state").by(equal);
                ProjectionOperation projection = Aggregation.project().and(filter).as("states");

                UnwindOperation unwindOperation = Aggregation.unwind("states");
                ProjectionOperation cityProjection = Aggregation.project().and("states.cities").as("cities");

                Aggregation aggregation = Aggregation.newAggregation(matchOperation, projection, unwindOperation,
                                cityProjection);

                return Optional.ofNullable(mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
                                State.class).getMappedResults().get(0));
        }
        default List<StateDTO> getStates(MongoTemplate mongoTemplate) {
                Aggregation aggregation = Aggregation.newAggregation(
                        Aggregation.unwind("states"),
                        Aggregation.project()
                                .and("states.name").as("name")
                                .and("states.stateCode").as("stateCode")
                );

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, StateDTO.class).getMappedResults();
        }


}
