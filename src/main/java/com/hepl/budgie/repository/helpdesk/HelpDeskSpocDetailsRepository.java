package com.hepl.budgie.repository.helpdesk;
import com.hepl.budgie.entity.helpdesk.HelpDeskSPOCDetails;

import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Objects;

@Repository

public interface HelpDeskSpocDetailsRepository extends MongoRepository<HelpDeskSPOCDetails, String> {

    public static final String COLLECTION_NAME = "m_helpdesk_spoc_details";

    public static final String SPOC_SEQUENCE = "HDSD0";

    default boolean checkSpocExists(String empId, MongoTemplate mongoTemplate) {
        Query query = new Query(Criteria.where("spocEmpId").is(empId));

        return mongoTemplate.count(query, COLLECTION_NAME) > 0;
    }

    default void addSpoc(MongoTemplate mongoTemplate, HelpDeskSPOCDetails helpDeskSPOCDetails, String org){
        Query menuQuery = new Query(Criteria.where("menuId").is(helpDeskSPOCDetails.getMenuId()));
        Document menuDoc = mongoTemplate.findOne(menuQuery, Document.class, "menus");

        if (menuDoc == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.MENU_NOT_FOUND);
        }

        List<Document> submenus = (List<Document>) menuDoc.get("submenus");

        if (submenus != null && !submenus.isEmpty()) {
            boolean subMenuExists = submenus.stream()
                    .anyMatch(sub -> helpDeskSPOCDetails.getSubMenuId() != null &&
                            helpDeskSPOCDetails.getSubMenuId().equals(sub.get("subMenuId")));

            if (!subMenuExists) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.INVALID_SUB_MENU);
            }
        } else {
            if (helpDeskSPOCDetails.getSubMenuId() != null && !helpDeskSPOCDetails.getSubMenuId().isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.MENU_NOT_FOUND);
            }
        }

        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);
        if(helpDeskSPOCDetails.getCommonId() == null || helpDeskSPOCDetails.getCommonId().isEmpty()){
            String nextCommonId = generateNextCommonId(mongoTemplate,org);
            helpDeskSPOCDetails.setCommonId(nextCommonId);
        }
        mongoTemplate.insert(helpDeskSPOCDetails, collectionName);
    }

    private String generateNextCommonId(MongoTemplate mongoTemplate, String org) {
        String collectionName = COLLECTION_NAME + (org.isEmpty() ? "" : "_" + org);

        Query query = new Query();
        query.fields().include("commonId");

        List<String> commonIdIds = mongoTemplate.find(query, HelpDeskSPOCDetails.class, collectionName)
                .stream()
                .map(HelpDeskSPOCDetails::getCommonId)
                .filter(Objects::nonNull)
                .toList();

        String lastSequence = commonIdIds.stream()
                .max(String::compareTo)
                .orElse(SPOC_SEQUENCE);

        return AppUtils.generateUniqueIdExpEdu(lastSequence, 2);
    }

//    static Update saveSpocDetails(HelpDeskSPOCDetails dto){
//        Update update = new Update()
//                .set("spocEmpId", dto.getSpocEmpId())
//                .set("menuId", dto.getMenuId())
//                .set("subMenuId", dto.getSubMenuId())
//                .set("_class", HelpDeskSPOCDetails.class.getName());
//        return update;
//    }
}
