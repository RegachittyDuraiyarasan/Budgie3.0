package com.hepl.budgie.repository.master;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.ModuleMaster;
import com.hepl.budgie.entity.organization.Organization;
import com.hepl.budgie.entity.role.Roles;
import com.hepl.budgie.utils.AppUtils;
import com.mongodb.client.result.UpdateResult;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.hepl.budgie.utils.AppUtils.generateUniqueIdExpEdu;

public interface ModuleMasterSettingsRepository extends MongoRepository<ModuleMaster, String> {

    public static final String COLLECTION_NAME = "module_m_settings";

    public static final String MODULE_SETTINGS_SEQUENCE = "MS000";

    Optional<ModuleMaster> findTopByOrderByIdDesc();
    

    Optional<ModuleMaster> findByReferenceName(String referenceName);

    default Optional<ModuleMaster> findByReferenceName(String referenceName, String org, MongoTemplate mongoTemplate) {
        String collectionName = "module_m_settings_" + org;
        Query query = new Query(Criteria.where("referenceName").is(referenceName));
        ModuleMaster result = mongoTemplate.findOne(query, ModuleMaster.class, collectionName);
        return Optional.ofNullable(result);
    }

    default boolean addOptions(Map<String, Object> option, MongoTemplate mongoTemplate, String referenceName,
            String org) {

        option.put("status", Status.ACTIVE.label);

        Query query = new Query(Criteria.where("referenceName").is(referenceName));

        Update update = new Update();
        update.push("options").each(option);

        // Generate next unique moduleId
        String nextModuleId = generateNextModuleId(mongoTemplate,referenceName, org);
        option.put("moduleId", nextModuleId);

        FindAndModifyOptions options = new FindAndModifyOptions();
        options.returnNew(true).upsert(true);

        UpdateResult result = mongoTemplate.upsert(query, update, ModuleMaster.class, getCollectionName(org));
        return result.wasAcknowledged();
    }

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + "_" + org);
    }

    default List<ModuleMaster> fetchOptions(MongoTemplate mongoTemplate, String referenceName, String org) {
        Query query = new Query(Criteria.where("referenceName").is(referenceName));

        List<ModuleMaster> documents = mongoTemplate.find(query, ModuleMaster.class, "module_m_settings_" + org);

        // Iterate over each document
        for (ModuleMaster document : documents) {
            List<Map<String, Object>> filteredOptions = document.getOptions().stream()
                    .filter(option -> Status.ACTIVE.label.equals(option.get("status")))
                    .collect(Collectors.toList());

            document.setOptions(filteredOptions);
        }

        return documents;
    }

    default boolean updateOptions(Map<String, Object> updatedOption, MongoTemplate mongoTemplate, String referenceName, String moduleId, String org) {
        if (updatedOption == null || updatedOption.isEmpty()) {
            return false; // No updates provided
        }

        Query query = new Query(Criteria.where("referenceName").is(referenceName)
                .and("options.moduleId").is(moduleId));

        Update update = new Update();
        for (Map.Entry<String, Object> entry : updatedOption.entrySet()) {
            update.set("options.$." + entry.getKey(), entry.getValue()); // Use positional operator $
        }

        UpdateResult result = mongoTemplate.updateFirst(query, update, ModuleMaster.class, getCollectionName(org));

        return result.getModifiedCount() > 0;
    }

    default UpdateResult deleteOptions(String moduleId, String referenceName, MongoTemplate mongoTemplate,
                                       String org) {
        Query query = new Query(Criteria.where("referenceName").is(referenceName)
                .and("options").elemMatch(Criteria.where("moduleId").is(moduleId)));

        Update update = new Update().set("options.$[elem].status", Status.DELETED.label);
        update.filterArray(Criteria.where("elem.moduleId").is(moduleId));

        return mongoTemplate.updateFirst(query, update, getCollectionName(org));
    }

    private String generateNextModuleId(MongoTemplate mongoTemplate, String referenceName, String org) {
        Query query = new Query(Criteria.where("referenceName").is(referenceName));
        query.fields().include("options.moduleId");

        // Fetch moduleIds for the given referenceName
        List<String> moduleIds = mongoTemplate.find(query, ModuleMaster.class, getCollectionName(org))
                .stream()
                .flatMap(module -> module.getOptions().stream())
                .map(option -> (String) option.get("moduleId"))
                .filter(Objects::nonNull)
                .toList();

        // Determine lastSequence safely
        String lastSequence = moduleIds.stream()
                .max(String::compareTo)
                .orElse(MODULE_SETTINGS_SEQUENCE);

        return AppUtils.generateUniqueIdExpEdu(lastSequence , 3);
    }

     default void initMastermoduleSettings(String org, MongoTemplate mongoTemplate) {
                mongoTemplate.indexOps(getCollectionName(org)).ensureIndex(
                                new Index("referenceName", Sort.Direction.ASC).unique().collation(Collation.of(Locale.US).strength(2)));

                Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(getCollectionName(org)));

                mongoTemplate.aggregate(aggregation, COLLECTION_NAME, ModuleMaster.class);
        }




}
