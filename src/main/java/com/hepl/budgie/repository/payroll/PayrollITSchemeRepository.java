package com.hepl.budgie.repository.payroll;

import java.util.*;
import java.util.stream.Collectors;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.dto.payroll.PayrollITSchemeDTO;
import com.hepl.budgie.dto.payroll.PayrollTypeDTO;
import com.hepl.budgie.entity.payroll.ITScheme;
import com.hepl.budgie.entity.payroll.PayrollITScheme;

@Repository
public interface PayrollITSchemeRepository extends MongoRepository<PayrollITScheme, String> {

    static final String COLLECTION_NAME = "payroll_it_scheme";

    default String getCollectionName(String country) {
        return country.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + "_" + country);
    }

    default PayrollITScheme findByType(String section, String orgId, MongoTemplate mongoTemplate, String country) {

        String collection = getCollectionName(country);
        Query query = new Query();
        query.addCriteria(Criteria.where("type").is(section).and("orgId").is(orgId));

        return mongoTemplate.findOne(query, PayrollITScheme.class, collection);
    }

    default PayrollITScheme savePayrollSection(String section, String description, String orgId,
            MongoTemplate mongoTemplate,
            String country) {

        String collection = getCollectionName(country);
        PayrollITScheme payrollITScheme = new PayrollITScheme();
        payrollITScheme.setType(section);
        payrollITScheme.setOrgId(orgId);
        payrollITScheme.setDescription(description);
        payrollITScheme.setStatus("Active");

        return mongoTemplate.save(payrollITScheme, collection);

    }

    default PayrollITScheme savePayrollScheme(PayrollITSchemeDTO scheme, String slug, String orgId,
            String schemeId, MongoTemplate mongoTemplate,
            String country) {
        String collection = getCollectionName(country);

        Query query = new Query(Criteria.where("type").is(scheme.getType()).and("orgId").is(orgId));

        ITScheme itScheme = new ITScheme();
        itScheme.setTitle(scheme.getTitle());
        itScheme.setShortName(scheme.getShortName());
        itScheme.setSlugName(slug);
        itScheme.setSchemeId(schemeId);
        itScheme.setMaxAmount(scheme.getMaxAmount());
        itScheme.setMaxAbove60(scheme.getMaxAbove60());
        itScheme.setMax60to80(scheme.getMax60to80());
        itScheme.setMaxAbove80(scheme.getMaxAbove80());
        itScheme.setStatus("Active");

        Update update = new Update()
                .push("schemes", itScheme)
                .set("schemeExists", "yes")
                .set("status", "Active");

        mongoTemplate.updateFirst(query, update, PayrollITScheme.class, collection);
        return mongoTemplate.findOne(query, PayrollITScheme.class, collection);
    }

    default String getNextITSchemeIdFromDB(String orgId, MongoTemplate mongoTemplate, String country) {

        String collection = getCollectionName(country);
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("orgId").is(orgId)),
                Aggregation.unwind("schemes"),
                Aggregation.project().and("schemes.schemeId").as("schemeId"),
                Aggregation.match(Criteria.where("schemeId").regex("^ITS\\d+$")));

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, collection,
                Document.class);

        int maxId = results.getMappedResults().stream()
                .map(doc -> doc.getString("schemeId"))
                .filter(Objects::nonNull)
                .map(id -> {
                    try {
                        return Integer.parseInt(id.substring(3));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max(Integer::compareTo)
                .orElse(0);

        return String.format("ITS%04d", maxId + 1);
    }

    default List<PayrollTypeDTO> getPayrollType(MongoTemplate mongoTemplate, String orgId, String country) {

        String collection = getCollectionName(country);
        Query query = new Query(Criteria.where("orgId").is(orgId));
        query.fields().include("type").include("description");

        List<PayrollITScheme> schemes = mongoTemplate.find(query, PayrollITScheme.class, collection);

        return schemes.stream()
                .map(scheme -> new PayrollTypeDTO(scheme.getType(), scheme.getDescription()))
                .collect(Collectors.toList());

    }

}
