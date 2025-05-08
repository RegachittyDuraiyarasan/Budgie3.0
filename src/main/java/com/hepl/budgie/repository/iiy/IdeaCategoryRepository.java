package com.hepl.budgie.repository.iiy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.IdeaCategory;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.mongodb.core.query.*;

@Repository
public interface IdeaCategoryRepository extends MongoRepository<IdeaCategory, String> {
    public static final String COLLECTION_NAME = "idea_category";

    default Optional<IdeaCategory> findTopByOrderByIdDesc(String organizationCode, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        IdeaCategory result = mongoTemplate.findOne(query, IdeaCategory.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void insertOrUpdate(IdeaCategory request, MongoTemplate mongoTemplate, String organizationCode,
            String authUser) {
        Query query = new Query(Criteria.where("ideaCategoryId").is(request.getIdeaCategoryId()));
        boolean isNew = !mongoTemplate.exists(query, IdeaCategory.class, getCollectionName(organizationCode));
        Update update = new Update();
        update.set("ideaCategoryName", request.getIdeaCategoryName());
        if (isNew) {
            update.setOnInsert("ideaCategoryId", request.getIdeaCategoryId());
            update.setOnInsert("status", Status.ACTIVE.label);
        }
        update = auditInfo(update, isNew, authUser);
        mongoTemplate.upsert(query, update, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default List<IdeaCategory> findByAll(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default Optional<Object> findByIdeaCategoryId(MongoTemplate mongoTemplate, String organizationCode,
            String ideaCategoryId) {
        Query query = new Query(
                Criteria.where("ideaCategoryId").is(ideaCategoryId).and("status").nin(Status.DELETED.label));
        IdeaCategory result = mongoTemplate.findOne(query, IdeaCategory.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void deleteByIdeaCategoryId(String id, MongoTemplate mongoTemplate, String organizationCode,
            String authUser) {
        Query query = new Query(Criteria.where("ideaCategoryId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default void updateByIdeaCategoryId(String id, MongoTemplate mongoTemplate, String organizationCode, String status,
            String authUser) {
        Query query = new Query(Criteria.where("ideaCategoryId").is(id));
        Update update = new Update();
        update.set("status", status);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default List<IdeaCategory> findAllByActiveStatus(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
        return mongoTemplate.find(query, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default boolean existsByIdeaCategoryName(MongoTemplate mongoTemplate, String organizationCode,
            String ideaCategoryName) {
        Query query = new Query(
                Criteria.where("ideaCategoryName").is(ideaCategoryName).and("status").nin(Status.DELETED.label));

        return mongoTemplate.exists(query, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default boolean existsByIdeaCategoryNameAndIdeaCategoryIdNot(MongoTemplate mongoTemplate, String organizationCode,
            String ideaCategoryName, String ideaCategoryId) {
        Query query = new Query(Criteria.where("ideaCategoryName").is(ideaCategoryName).and("status")
                .nin(Status.DELETED.label).and("ideaCategoryId").ne(ideaCategoryId));

        return mongoTemplate.exists(query, IdeaCategory.class, getCollectionName(organizationCode));
    }

    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }

    default Update auditInfo(Update update, boolean isNew, String authUser) {

        if (isNew) {
            update.setOnInsert("createdDate", LocalDateTime.now());
            update.setOnInsert("createdByUser", authUser);
        }
        update.set("lastModifiedDate", LocalDateTime.now());
        update.set("modifiedByUser", authUser);
        return update;
    }

}
