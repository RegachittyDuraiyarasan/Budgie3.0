package com.hepl.budgie.repository.iiy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.hepl.budgie.entity.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.iiy.CourseCategory;
import org.springframework.data.mongodb.core.query.*;

@Repository
public interface CourseCategoryRepository extends MongoRepository<CourseCategory, String> {
    public static final String COLLECTION_NAME = "course_category";

    default Optional<CourseCategory> findTopByOrderByIdDesc(String organizationCode, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        CourseCategory result = mongoTemplate.findOne(query, CourseCategory.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void insertOrUpdate(CourseCategory request, MongoTemplate mongoTemplate, String organizationCode,
            String authUser) {
        Query query = new Query(Criteria.where("categoryId").is(request.getCategoryId()));
        boolean isNew = !mongoTemplate.exists(query, CourseCategory.class, getCollectionName(organizationCode));
        Update update = new Update();
        update.set("categoryName", request.getCategoryName());
        if (isNew) {
            update.setOnInsert("categoryId", request.getCategoryId());
            update.setOnInsert("status", Status.ACTIVE.label);
        }
        update = auditInfo(update, isNew, authUser);
        mongoTemplate.upsert(query, update, CourseCategory.class, getCollectionName(organizationCode));
    }

    default List<CourseCategory> findByAll(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, CourseCategory.class, getCollectionName(organizationCode));
    }

    default Optional<Object> findByCategoryId(MongoTemplate mongoTemplate, String organizationCode, String categoryId) {
        Query query = new Query(Criteria.where("categoryId").is(categoryId).and("status").nin(Status.DELETED.label));
        CourseCategory result = mongoTemplate.findOne(query, CourseCategory.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void deleteByCategoryId(String id, MongoTemplate mongoTemplate, String organizationCode, String authUser) {
        Query query = new Query(Criteria.where("categoryId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, CourseCategory.class, getCollectionName(organizationCode));
    }

    default void updateByCategoryId(String id, MongoTemplate mongoTemplate, String organizationCode, String status,
            String authUser) {
        Query query = new Query(Criteria.where("categoryId").is(id));
        Update update = new Update();
        update.set("status", status);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, CourseCategory.class, getCollectionName(organizationCode));
    }

    default List<CourseCategory> findAllByActiveStatus(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
        return mongoTemplate.find(query, CourseCategory.class, getCollectionName(organizationCode));
    }

    default boolean existsByCategoryName(MongoTemplate mongoTemplate, String organizationCode, String categoryName) {
        Query query = new Query(
                Criteria.where("categoryName").is(categoryName).and("status").nin(Status.DELETED.label));

        return mongoTemplate.exists(query, CourseCategory.class, getCollectionName(organizationCode));
    }

    default boolean existsByCategoryNameAndCategoryIdNot(MongoTemplate mongoTemplate, String organizationCode,
            String categoryName, String categoryId) {
        Query query = new Query(Criteria.where("categoryName").is(categoryName).and("status").nin(Status.DELETED.label)
                .and("categoryId").ne(categoryId));

        return mongoTemplate.exists(query, CourseCategory.class, getCollectionName(organizationCode));
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
