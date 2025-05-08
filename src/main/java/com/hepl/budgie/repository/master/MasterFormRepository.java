package com.hepl.budgie.repository.master;

import com.hepl.budgie.dto.form.FormFieldsDTO;
import com.hepl.budgie.dto.form.FormDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.MasterForm;
import com.mongodb.client.result.UpdateResult;

import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AddFieldsOperation;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationPipeline;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.ArrayOperators;
import org.springframework.data.mongodb.core.aggregation.BooleanOperators;
import org.springframework.data.mongodb.core.aggregation.ComparisonOperators;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.LookupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.UnwindOperation;
import org.springframework.data.mongodb.core.aggregation.ConditionalOperators.IfNull;
import org.springframework.data.mongodb.core.aggregation.VariableOperators.Let.ExpressionVariable;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Map;
import java.util.List;
import java.util.Optional;

public interface MasterFormRepository extends MongoRepository<MasterForm, String> {

        public static final String COLLECTION_NAME = "m_forms";
        public static final String REF_COLLECTION_NAME = "m_settings";

        @org.springframework.data.mongodb.repository.Query("{'name': ?0}") // Custom query to search by 'name'
        Optional<MasterForm> findByName(String name);

        default List<MasterForm> getAll(String org, MongoTemplate mongoTemplate) {
                return mongoTemplate.findAll(MasterForm.class, getCollectionName(org));
        }

        default void deleteFormByFieldId(String formId, String fieldId, String org, MongoTemplate mongoTemplate) {
                Query query = new Query(
                                Criteria.where("_id").is(formId));
                Update update = new Update();
                update.pull("formFields", new Query(Criteria.where("fieldId").is(fieldId)));

                mongoTemplate.updateFirst(query, update, getCollectionName(org));
        }

        default void deleteForms(String formId, String org, MongoTemplate mongoTemplate) {
                Query query = new Query(
                                Criteria.where("_id").is(formId));
                mongoTemplate.remove(query, getCollectionName(org));
        }

        default void initMasterForms(String org, MongoTemplate mongoTemplate) {
                mongoTemplate.indexOps(getCollectionName(org)).ensureIndex(
                                new Index("formName", Sort.Direction.ASC).unique());

                Aggregation aggregation = Aggregation.newAggregation(Aggregation.out(getCollectionName(org)));

                mongoTemplate.aggregate(aggregation, COLLECTION_NAME, MasterForm.class);
        }

        @org.springframework.data.mongodb.repository.Query(value = "{}", fields = "{'formName': 1, 'formType': 1}")
        List<MasterForm> findAllLimitedFields();

        default AggregationResults<FormDTO> fetchByFormName(String formName, String role, String type,
                        MongoTemplate mongoTemplate, String org) {
                MatchOperation formMatchOperation = Aggregation.match(Criteria.where("formName").is(formName));
                UnwindOperation unwindFieldsOperation = Aggregation.unwind("formFields");

                ComparisonOperators.Eq deletedEqOperator = ComparisonOperators.Eq.valueOf("$$option.status")
                                .equalToValue(Status.ACTIVE.label);
                ProjectionOperation settingOptionOperation = Aggregation.project("referenceName").and("options")
                                .filter("option", deletedEqOperator).as("options");
                MatchOperation pipMatchOperation = Aggregation
                                .match(Criteria
                                                .expr(
                                                                ComparisonOperators.Eq.valueOf("$$settingId")
                                                                                .equalTo("_id")));
                LookupOperation lookupSettingsOperation = Aggregation.lookup()
                                .from(REF_COLLECTION_NAME + (org.isEmpty() ? "" : ('_' + org)))
                                .let(ExpressionVariable.newVariable("settingId")
                                                .forField("formFields.optionsReference.$id"))
                                .pipeline(AggregationPipeline.of(pipMatchOperation, settingOptionOperation))
                                .as("formFields.optionsReference");

                MatchOperation accessLevelOperation = Aggregation
                                .match(Criteria.where("formFields.accessLevel.role").in(role));

                ArrayOperators.In inOperators = ArrayOperators.In.arrayOf("$$access.role").containsValue(role);
                ComparisonOperators.Eq typeEqOperator = ComparisonOperators.Eq.valueOf("$$access.type")
                                .equalToValue(type);
                BooleanOperators.And andOperators = BooleanOperators.And.and(inOperators, typeEqOperator);

                ArrayOperators.Filter filterOperators = ArrayOperators.Filter.filter("formFields.accessLevel")
                                .as("access")
                                .by(andOperators);
                ArrayOperators.First firstOperators = ArrayOperators.First.firstOf(filterOperators);

                AddFieldsOperation addFieldsOperation = Aggregation.addFields().addField("formFields.accessLevel")
                                .withValueOf(firstOperators).build();

                ArrayOperators.First firstOptionsOperators = ArrayOperators.First
                                .firstOf("formFields.optionsReference");

                ProjectionOperation projectionOperation = Aggregation
                                .project("formName", "formType", "apiFlow", "initialValue", "workflow")
                                .and("formFields.fieldId")
                                .as("formFields.fieldId")
                                .and("formFields.btnAction")
                                .as("formFields.btnAction")
                                .and("formFields.fieldName")
                                .as("formFields.fieldName").and("formFields.validation")
                                .as("formFields.validation").and("formFields.type")
                                .as("formFields.type").and("formFields.multiple")
                                .as("formFields.multiple").and("formFields.dependencies")
                                .as("formFields.dependencies").and("formFields.attribute")
                                .as("formFields.attribute").and("formFields.placeholder")
                                .as("formFields.placeholder").and("formFields.position")
                                .as("formFields.position").and("formFields.optionsReferenceLink")
                                .as("formFields.optionsReferenceLink").and("formFields.optionsFilterFrom")
                                .as("formFields.optionsFilterFrom").and("formFields.optionsDefault")
                                .as("formFields.optionsDefault").and("formFields.acceptedFileFormats")
                                .as("formFields.acceptedFileFormats")
                                .and(firstOptionsOperators).as("formFields.optionsReference")
                                .and("formFields.accessLevel.show").as("formFields.show")
                                .and("formFields.accessLevel.disabled").as("formFields.disabled")
                                .and("formFields.accessLevel.required").as("formFields.required");

                IfNull ifNullOperator = IfNull.ifNull("formFields.optionsReference")
                                .thenValueOf("formFields.optionsDefault");
                AddFieldsOperation addDefaultOptionOperation = Aggregation.addFields()
                                .addField("formFields.optionsReference")
                                .withValueOf(ifNullOperator).build();

                ConditionalOperators.Cond buttonCaseEq = ConditionalOperators.Cond
                                .when(ComparisonOperators.Eq.valueOf("formFields.type").equalToValue("button"))
                                .then("$formFields").otherwise("$$REMOVE");
                ConditionalOperators.Cond buttonCaseNe = ConditionalOperators.Cond
                                .when(ComparisonOperators.Ne.valueOf("formFields.type").notEqualToValue("button"))
                                .then("$formFields").otherwise("$$REMOVE");

                GroupOperation groupOperation = Aggregation.group("_id").first("apiFlow").as("apiFlow")
                                .first("workflow").as("workflow")
                                .first("initialValue").as("initialValue")
                                .first("formName").as("formName").first("formType")
                                .as("formType").push(buttonCaseNe)
                                .as("formFields").push(buttonCaseEq)
                                .as("buttons");

                Aggregation aggregation = Aggregation.newAggregation(formMatchOperation, unwindFieldsOperation,
                                lookupSettingsOperation,
                                accessLevelOperation, addFieldsOperation, projectionOperation,
                                addDefaultOptionOperation, groupOperation);

                return mongoTemplate.aggregate(aggregation, getCollectionName(org),
                                FormDTO.class);
        }

        default void saveForms(Map<String, Object> formRequestDTO, MongoTemplate mongoTemplate, String org) {

                mongoTemplate.save(formRequestDTO, getCollectionName(org));
        }

        default void updateForms(Map<String, Object> formRequestDTO, MongoTemplate mongoTemplate, String formId,
                        String org) {

                Query query = new Query(
                                Criteria.where("_id").is(formId));

                mongoTemplate.replace(query, formRequestDTO, getCollectionName(org));
        }

        default UpdateResult updateFormField(FormFieldsDTO fields, String formId, MongoTemplate mongoTemplate,
                        String org) {

                Query query = new Query(
                                Criteria.where("_id").is(formId).and("formFields.fieldId").is(fields.getFieldId()));

                Update update = new Update();
                update.set("initialValue." + fields.getFieldId(), fields.getInitialValue());
                update.set("formFields.$.fieldName", fields.getFieldName());
                update.set("formFields.$.type", fields.getType());
                update.set("formFields.$.multiple", fields.getMultiple());
                update.set("formFields.$.placeholder", fields.getPlaceholder());
                update.set("formFields.$.validation", fields.getValidation());
                update.set("formFields.$.position", fields.getPosition());
                update.set("formFields.$.attribute", fields.getAttribute());
                update.set("formFields.$.dependencies", fields.getDependencies());
                update.set("formFields.$.optionsReferenceLink", fields.getOptionsReferenceLink());
                update.set("formFields.$.accessLevel", fields.getAccessLevel());
                if (!StringUtils.isBlank(fields.getOptionsReferenceId())) {
                        update.set("formFields.$.optionsReference", new com.mongodb.DBRef(
                                        REF_COLLECTION_NAME + (org.isEmpty() ? "" : ('_' + org)),
                                        new ObjectId(fields.getOptionsReferenceId())));
                } else {
                        update.unset("formFields.$.optionsReference");
                }

                return mongoTemplate.updateFirst(query, update, getCollectionName(org));

        }

        default void saveGenericCollection(Map<String, Object> fields, String filter, String collectionName,
                        MongoTemplate mongoTemplate) {

        }

        Optional<MasterForm> findByFormName(String addNewSpoc);

        default String getCollectionName(String org) {
                return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
        }
}
