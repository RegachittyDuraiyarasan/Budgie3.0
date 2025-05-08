package com.hepl.budgie.repository;

import com.hepl.budgie.entity.FinancialYear;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FinancialYearRepository extends MongoRepository<FinancialYear, String> {

    boolean existsByCountry(String country);

    Optional<FinancialYear> findByCountry(String india);
}
