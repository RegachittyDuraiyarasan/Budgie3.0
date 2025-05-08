package com.hepl.budgie.repository.master;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.entity.userinfo.ProbationSettings;
import com.hepl.budgie.utils.AppMessages;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.BulkOperations;
import org.springframework.data.mongodb.core.BulkOperations.BulkMode;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterForm;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.mongodb.client.result.UpdateResult;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

public interface MasterSettingsRepository extends MongoRepository<MasterFormSettings, String> {

    public static final String COLLECTION_NAME = "m_settings";

    // default void initMasterSettings(String organisation, MongoTemplate mongoTemplate) {
    //     mongoTemplate.indexOps(getCollectionName(organisation)).ensureIndex(
    //             new Index("referenceName", Sort.Direction.ASC).unique());

    //     Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(getCollectionName(organisation)));

    //     mongoTemplate.aggregate(aggregation, COLLECTION_NAME, MasterFormSettings.class);
    // }

    
          

        default void initMasterSettings(String org,MongoTemplate mongoTemplate){
            mongoTemplate.indexOps(getCollectionName(org)).ensureIndex(
                             new Index("referenceName", Sort.Direction.ASC).unique().collation(Collation.of(Locale.US).strength(2)));

            Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(getCollectionName(org)));

            mongoTemplate.aggregate(aggregation, COLLECTION_NAME, MasterFormSettings.class);
        }

    default Optional<MasterFormSettings> fetchOptions(String referenceName, String org, MongoTemplate mongoTemplate) {
        MatchOperation matchStage = Aggregation.match(Criteria.where("referenceName").is(referenceName));

        ProjectionOperation projectStage = Aggregation.project()
                .andInclude("referenceName")
                .and(ArrayOperators.Filter.filter("options")
                        .as("option")
                        .by(AggregationSpELExpression.expressionOf("option.status != 'Deleted'")))
                .as("options");

        // Combine match and projection stages
        Aggregation aggregation = Aggregation.newAggregation(matchStage, projectStage);

        AggregationResults<MasterFormSettings> results = mongoTemplate.aggregate(aggregation, getCollectionName(org),
                MasterFormSettings.class);

        List<MasterFormSettings> mappedResults = results.getMappedResults();
        return mappedResults.isEmpty() ? Optional.empty() : Optional.of(mappedResults.get(0));
    }

    default UpdateResult deleteOptions(String optionId, String referenceName, MongoTemplate mongoTemplate,
            JWTHelper jwtHelper) {
        Query query = new Query(Criteria.where("referenceName").is(referenceName)
                .and("options").elemMatch(Criteria.where("value").is(optionId)));

        Update update = new Update()
                .set("options.$[elem].status", Status.DELETED.label)
                .set("options.$[elem].updatedAt", ZonedDateTime.now())
                .set("options.$[elem].updatedBy", jwtHelper.getUserRefDetail().getEmpId());
        update.filterArray(Criteria.where("elem.value").is(optionId));

        return mongoTemplate.updateFirst(query, update, getCollectionName(jwtHelper.getOrganizationCode()));
    }

    default void upsertSettings(MongoTemplate mongoTemplate, List<String> masters,
            java.util.Map<String, List<String>> predefinedMasters, String organisation) {

        BulkOperations operations = mongoTemplate.bulkOps(BulkMode.UNORDERED, getCollectionName(organisation));
        for (String master : masters) {
            Query query = new Query(Criteria.where("referenceName").is(master));

            Update update = new Update();
            update.setOnInsert("referenceName", master);
            update.setOnInsert("options", new ArrayList<>());

            operations.upsert(query, update);
        }
        for (java.util.Map.Entry<String, List<String>> entry : predefinedMasters.entrySet()) {
            Query query = new Query(Criteria.where("referenceName").is(entry.getKey()));

            List<MasterFormOptions> options = entry.getValue().stream().map(e -> {
                MasterFormOptions option = new MasterFormOptions();
                option.setName(e);
                option.setValue(java.util.UUID.randomUUID().toString());
                return option;
            }).toList();

            Update update = new Update();
            update.setOnInsert("referenceName", entry.getKey());
            update.setOnInsert("options", options);

            operations.upsert(query, update);
        }

        operations.execute();
    }

    default Optional<MasterFormSettings> addOptions(MasterFormOptions option, MongoTemplate mongoTemplate,
            String referenceName, JWTHelper jwtHelper) {
        String collectionName = getCollectionName(jwtHelper.getOrganizationCode());

        Query query = new Query(Criteria.where("referenceName").is(referenceName));
        MasterFormSettings existingSettings = mongoTemplate.findOne(query, MasterFormSettings.class, collectionName);

        if (existingSettings != null && existingSettings.getOptions() != null) {
            for (MasterFormOptions existingOption : existingSettings.getOptions()) {

                if (existingOption.getName().equalsIgnoreCase(option.getName())) {

                    if ("Active".equalsIgnoreCase(existingOption.getStatus())) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.NAME_ALREADY_EXISTS);
                    }

                    if ("Deleted".equalsIgnoreCase(existingOption.getStatus())) {
                        existingOption.setStatus("Active");
                        Update update = new Update();
                        update.set("options", existingSettings.getOptions()); 
                        mongoTemplate.findAndModify(query, update, MasterFormSettings.class, collectionName);
                        return Optional.of(existingSettings);
                    }
                }
            }
        }

        Update update = new Update();
        update.push("options", option);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true);

        return Optional.ofNullable(
                mongoTemplate.findAndModify(query, update, options, MasterFormSettings.class, collectionName));
    }

    default Optional<MasterFormSettings> updateOption(String value, String referenceName,
            MasterFormOptions updatedOption, MongoTemplate mongoTemplate, JWTHelper jwtHelper) {
        String collectionName = COLLECTION_NAME
                + (jwtHelper.getOrganizationCode().isEmpty() ? "" : "_" + jwtHelper.getOrganizationCode()); // Dynamic
                                                                                                            // collection
                                                                                                            // based on
                                                                                                            // org

        Query query = new Query(Criteria.where("referenceName").is(referenceName).and("options.value").is(value));

        Update update = new Update();
        Optional<ProbationSettings> details = Optional.ofNullable(updatedOption.getProbationDetail());
        if (details.isPresent()) {
            update.set("options.$.isProbationRequired", details.get().isProbationRequired());
            update.set("options.$.defaultDurationMonths", details.get().getDefaultDurationMonths());
            update.set("options.$.extensionOptionsMonths", details.get().getExtensionOptionsMonths());
        }
        if (updatedOption.getNoticePeriod() != null) {
            update.set("options.$.noticePeriod", updatedOption.getNoticePeriod());
        }
        if (updatedOption.getMobileNumber() != null) {
            update.set("options.$.mobileNumber", updatedOption.getMobileNumber());
        }
        if (updatedOption.getEmail() != null) {
            update.set("options.$.email", updatedOption.getEmail());
        }
        if (updatedOption.getName() != null) {
            update.set("options.$.name", updatedOption.getName());
            update.set("options.$.value", updatedOption.getName());
        }
        if (updatedOption.getSeries() != null) {
            update.set("options.$.series", updatedOption.getSeries());
        }
        if (updatedOption.getProbationStatus() != null) {
            update.set("options.$.probationStatus", updatedOption.getProbationStatus());
        }
        if (updatedOption.getEligibleForITDeclaration() != null) {
            update.set("options.$.eligibleForITDeclaration", updatedOption.getEligibleForITDeclaration());
        }
        update.set("options.$.status", Status.ACTIVE.label);
        update.set("options.$.updatedAt", ZonedDateTime.now());
        update.set("options.$.updatedBy", jwtHelper.getUserRefDetail().getEmpId());
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return Optional.ofNullable(
                mongoTemplate.findAndModify(query, update, options, MasterFormSettings.class, collectionName));
    }

    default AggregationResults<MasterFormOptions> findByGrade(String name, MongoTemplate mongoTemplate, String org) {

        MatchOperation matchOperation = Aggregation.match(Criteria.where("referenceName").is("Grade"));
        UnwindOperation unwindOperation = Aggregation.unwind("options");
        MatchOperation matchOptionOperation = Aggregation.match(Criteria.where("options.name").is(name));

        ProjectionOperation projectionOperation = Aggregation.project().and("options.name").as("name")
                .and("options.value").as("value")
                .and("options.noticePeriod").as("noticePeriod")
                .and("options.status").as("status")
                .and("options.probationDetail").as("probationDetail");

        Aggregation aggregation = Aggregation.newAggregation(matchOperation, unwindOperation,
                matchOptionOperation, projectionOperation);
        return mongoTemplate.aggregate(aggregation, getCollectionName(org), MasterFormOptions.class);
    }

    default AggregationResults<OptionsResponseDTO> getOptions(MongoTemplate mongoTemplate, String org) {

        ProjectionOperation projectionOperation = Aggregation.project().and("_id").as("value")
                .and("referenceName").as("name");

        Aggregation aggregation = Aggregation.newAggregation(projectionOperation);
        return mongoTemplate.aggregate(aggregation, getCollectionName(org),
                OptionsResponseDTO.class);
    }

    default Optional<MasterFormSettings> updateStatus(String value, String referenceName, String status,
            MongoTemplate mongoTemplate, String org) {
        Query query = new Query(Criteria.where("referenceName").is(referenceName).and("options.value").is(value));

        Update update = new Update();
        update.set("options.$.status", status); // Update status dynamically from request

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);
        return Optional.ofNullable(
                mongoTemplate.findAndModify(query, update, options, MasterFormSettings.class, getCollectionName(org)));
    }

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default MasterFormSettings findByReferenceName(String referenceName, MongoTemplate mongoTemplate, String org) {
        String cln = getCollectionName(org);
        Query query = new Query(Criteria.where("referenceName").is(referenceName));
        return mongoTemplate.findOne(query, MasterFormSettings.class, cln);
    }
}
