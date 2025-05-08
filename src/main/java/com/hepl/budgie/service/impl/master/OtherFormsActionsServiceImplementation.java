package com.hepl.budgie.service.impl.master;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.service.master.OtherFormsActionsService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.BasicDBObject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OtherFormsActionsServiceImplementation implements OtherFormsActionsService {
    private final MongoTemplate mongoTemplate;

    public OtherFormsActionsServiceImplementation(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void saveDynamicData(FormRequest formRequest, String filter) {
        String formName = formRequest.getFormName();
        Map<String, Map<String, String>> collectionFieldsMap = fetchCollectionFieldsMap(formName, formRequest);

        if (collectionFieldsMap.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        Map<String, Object> formFields = formRequest.getFormFields();

        // Iterate over each collection and save only if formFields fieldId matches collectionFieldId in collectionMap
        for (Map.Entry<String, Map<String, String>> collectionEntry : collectionFieldsMap.entrySet()) {
            String collectionName = collectionEntry.getKey();
            Map<String, String> allowedFieldToCollectionFieldIdMap = collectionEntry.getValue();

            Map<String, Object> validFields = new HashMap<>();
            for (Map.Entry<String, Object> formEntry : formFields.entrySet()) {
                String fieldId = formEntry.getKey();
                Object fieldValue = formEntry.getValue();

                // Check if the fieldId matches any collectionFieldId in the collectionMap
                String collectionFieldId = allowedFieldToCollectionFieldIdMap.get(fieldId);
                if (collectionFieldId != null) {
                    validFields.put(collectionFieldId, fieldValue);
                }
            }

            // Save to database if there are any valid fields
            if (!validFields.isEmpty()) {
                BasicDBObject document = new BasicDBObject();
                document.putAll(validFields);
                mongoTemplate.save(document, collectionName);
            }
        }
    }


    private Map<String, Map<String, String>> fetchCollectionFieldsMap(String formName, FormRequest formRequest) {
        Query query = new Query(Criteria.where("formName").is(formName));
        Map<String, Object> formDefinition = mongoTemplate.findOne(query, Map.class, "m_forms");

        if (formDefinition == null || !formDefinition.containsKey("collectionMap") || !formDefinition.containsKey("formFields")) {
            log.error("No collection map or form fields found for formName: {}", formName);
            return Collections.emptyMap();
        }

        List<Map<String, Object>> collectionMap = (List<Map<String, Object>>) formDefinition.get("collectionMap");
        List<Map<String, Object>> formFields = (List<Map<String, Object>>) formDefinition.get("formFields");

        // Create a map of fieldId to collectionFieldId from formFields
        Map<String, String> fieldIdToCollectionFieldId = formFields.stream()
                .filter(field -> field.containsKey("fieldId") && field.containsKey("collectionFieldId"))
                .collect(Collectors.toMap(
                        field -> (String) field.get("fieldId"),
                        field -> (String) field.get("collectionFieldId")
                ));

        Map<String, Map<String, String>> collectionFieldsMap = new HashMap<>();

        // Process each collection in collectionMap
        for (Map<String, Object> collection : collectionMap) {
            String collectionName = (String) collection.get("collectionName");
            if (collectionName != null) {
                Map<String, String> fieldToCollectionFieldIdMap = new HashMap<>();

                // Iterate over each entry in fieldIdToCollectionFieldId
                for (Map.Entry<String, String> entry : fieldIdToCollectionFieldId.entrySet()) {
                    String fieldId = entry.getKey();
                    String collectionFieldId = entry.getValue();

                    // Check if collection contains the collectionFieldId
                    if (collection.containsKey(collectionFieldId)) {
                        fieldToCollectionFieldIdMap.put(fieldId, collectionFieldId);
                    }
                }

                if (!fieldToCollectionFieldIdMap.isEmpty()) {
                    collectionFieldsMap.put(collectionName, fieldToCollectionFieldIdMap);
                    log.info("collectionFieldsMap for {}: {}", collectionName, fieldToCollectionFieldIdMap);
                }
            }
        }
        return collectionFieldsMap;
    }

    @Override
    public void updateDynamicData(String documentId, FormRequest formRequest) {
        String formName = formRequest.getFormName();
        Map<String, Map<String, String>> collectionFieldsMap = fetchCollectionFieldsMap(formName, formRequest);

        if (collectionFieldsMap.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        Map<String, Object> formFields = formRequest.getFormFields();

        // Iterate over each collection and update
        for (Map.Entry<String, Map<String, String>> collectionEntry : collectionFieldsMap.entrySet()) {
            String collectionName = collectionEntry.getKey();
            Map<String, String> allowedFieldToCollectionFieldIdMap = collectionEntry.getValue();

            Map<String, Object> validFields = new HashMap<>();
            for (Map.Entry<String, Object> formEntry : formFields.entrySet()) {
                String fieldId = formEntry.getKey();
                Object fieldValue = formEntry.getValue();

                // Check if the fieldId matches any collectionFieldId in the collectionMap
                String collectionFieldId = allowedFieldToCollectionFieldIdMap.get(fieldId);
                if (collectionFieldId != null) {
                    validFields.put(collectionFieldId, fieldValue);
                }
            }

            // Update to database if there are any valid fields
            if (!validFields.isEmpty()) {
                // Update existing document by ID
                Query query = new Query(Criteria.where("_id").is(new ObjectId(documentId)));
                Update update = new Update();

                // Prepare the update fields
                for (Map.Entry<String, Object> entry : validFields.entrySet()) {
                    update.set(entry.getKey(), entry.getValue());
                }

                // Perform the update
                mongoTemplate.updateFirst(query, update, collectionName);
            }
        }
    }
}