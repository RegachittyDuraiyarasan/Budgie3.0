package com.hepl.budgie.repository.userinfo;

import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.utils.MongoExpressionHelper;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserExpEducationRepository extends MongoRepository<UserExpEducation, String> {

        public static final String COLLECTION_NAME = "user_exp_education";

        Optional<UserExpEducation> findByEmpId(String empId);

        default List<EmployeeActiveDTO> getEmployeeExperienceDetails(List<String> employees,
                        MongoTemplate mongoTemplate) {
                MatchOperation matchOperation = Aggregation.match(Criteria.where("empId").in(employees));
                UnwindOperation unwindOperation = Aggregation.unwind("experienceDetails");
                ProjectionOperation projectionOperation = Aggregation.project("empId")
                                .and(MongoExpressionHelper.dateDiff("experienceDetails.endOn",
                                                "experienceDetails.beginOn", "year"))
                                .as("years")
                                .and(MongoExpressionHelper.dateDiff("experienceDetails.endOn",
                                                "experienceDetails.beginOn", "month"))
                                .as("months")
                                .and(MongoExpressionHelper.dateDiff("experienceDetails.endOn",
                                                "experienceDetails.beginOn", "day"))
                                .as("days");
                GroupOperation groupOperation = Aggregation.group("empId").first("empId").as("empId").sum("years")
                                .as("years")
                                .sum("months").as("months").sum("days").as("days");

                Aggregation aggregation = Aggregation.newAggregation(matchOperation, unwindOperation,
                                projectionOperation, groupOperation);

                return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, EmployeeActiveDTO.class)
                                .getMappedResults();
        }
}
