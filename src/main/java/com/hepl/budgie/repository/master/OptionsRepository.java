package com.hepl.budgie.repository.master;

import com.hepl.budgie.dto.form.OptionsResponseDTO;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.utils.AppUtils;

import java.util.List;
import java.util.ArrayList;

public interface OptionsRepository extends MongoRepository<MasterForm, String> {

    default List<OptionsResponseDTO> getOptions(String name, String value, String collectionName,
            String filter, MongoTemplate mongoTemplate) {
        String[] fieldParts = name.split(",");

        StringOperators.Concat concatExpression = StringOperators.Concat.valueOf(fieldParts[0]);
        List<Criteria> criteriaList = new ArrayList<>();

        if (filter != null) {
            String[] filterArray = filter.split(",");
            for (String query : filterArray) {
                String[] fields = query.split(":");
                criteriaList.add(Criteria.where(fields[0]).is(AppUtils.typeConversionValue(fields[1], fields[2])));
            }
        }

        // Append fields or strings to the concat expression
        for (int i = 1; i < fieldParts.length; i++) {
            String part = fieldParts[i];
            concatExpression = concatExpression.concat(part);
        }

        ProjectionOperation project = Aggregation.project()
                .and(concatExpression).as("name")
                .and(value).as("value")
                .andExclude("_id");

        // Build and execute the aggregation query
        if (criteriaList.isEmpty()) {
            Aggregation aggregation = Aggregation.newAggregation(project);
            AggregationResults<OptionsResponseDTO> result = mongoTemplate.aggregate(aggregation, collectionName,
                    OptionsResponseDTO.class);
            return result.getMappedResults();
        } else {
            MatchOperation matchOperation = Aggregation.match(new Criteria().andOperator(criteriaList));
            Aggregation aggregation = Aggregation.newAggregation(matchOperation, project);
            AggregationResults<OptionsResponseDTO> result = mongoTemplate.aggregate(aggregation, collectionName,
                    OptionsResponseDTO.class);
            return result.getMappedResults();
        }
    }

}
