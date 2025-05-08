package com.hepl.budgie.repository.iiy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.hepl.budgie.entity.Status;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.hepl.budgie.entity.iiy.Course;
import org.springframework.data.mongodb.core.query.*;

@Repository
public interface CourseRepository extends MongoRepository<Course, String> {
    public static final String COLLECTION_NAME = "course";

    default Optional<Course> findTopByOrderByIdDesc(String organizationCode, MongoTemplate mongoTemplate) {
        Query query = new Query();
        query.with(Sort.by(Sort.Direction.DESC, "id"));
        query.limit(1);
        Course result = mongoTemplate.findOne(query, Course.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void insertOrUpdate(Course request, MongoTemplate mongoTemplate, String organizationCode, String authUser) {
        Query query = new Query(Criteria.where("courseId").is(request.getCourseId()));
        boolean isNew = !mongoTemplate.exists(query, Course.class, getCollectionName(organizationCode));
        Update update = new Update();
        update.set("courseName", request.getCourseName());
        update.set("category", request.getCategory());
        update.set("allDepartment", request.getAllDepartment());
        update.set("allEmployee", request.getAllEmployee());
        update.set("department", request.getDepartment());
        update.set("employee", request.getEmployee());
        if (isNew) {
            update.setOnInsert("courseId", request.getCourseId());
            update.setOnInsert("status", Status.ACTIVE.label);
        }
        update = auditInfo(update, isNew, authUser);
        mongoTemplate.upsert(query, update, Course.class, getCollectionName(organizationCode));
    }

    default List<Course> findByAll(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").nin(Status.DELETED.label));
        return mongoTemplate.find(query, Course.class, getCollectionName(organizationCode));
    }

    default Optional<Object> findByCourseId(MongoTemplate mongoTemplate, String organizationCode, String courseId) {
        Query query = new Query(Criteria.where("courseId").is(courseId).and("status").nin(Status.DELETED.label));
        Course result = mongoTemplate.findOne(query, Course.class, getCollectionName(organizationCode));
        return Optional.ofNullable(result);
    }

    default void deleteCourseId(String id, MongoTemplate mongoTemplate, String organizationCode, String authUser) {
        Query query = new Query(Criteria.where("courseId").is(id));
        Update update = new Update();
        update.set("status", Status.DELETED.label);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, Course.class, getCollectionName(organizationCode));
    }

    default void updateCourseId(String id, MongoTemplate mongoTemplate, String organizationCode, String status,
            String authUser) {
        Query query = new Query(Criteria.where("courseId").is(id));
        Update update = new Update();
        update.set("status", status);
        update = auditInfo(update, false, authUser);
        mongoTemplate.findAndModify(query, update, Course.class, getCollectionName(organizationCode));
    }

    default List<Course> findAllByActiveStatus(MongoTemplate mongoTemplate, String organizationCode) {
        Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
        return mongoTemplate.find(query, Course.class, getCollectionName(organizationCode));
    }

    default boolean existsByCourseName(MongoTemplate mongoTemplate, String organizationCode, String courseName) {
        Query query = new Query(Criteria.where("courseName").is(courseName).and("status").nin(Status.DELETED.label));

        return mongoTemplate.exists(query, Course.class, getCollectionName(organizationCode));
    }

    default boolean existsByCourseNameAndCourseIdNot(MongoTemplate mongoTemplate, String organizationCode,
            String courseName, String courseId) {
        Query query = new Query(Criteria.where("courseName").is(courseName).and("status").nin(Status.DELETED.label)
                .and("courseId").ne(courseId));

        return mongoTemplate.exists(query, Course.class, getCollectionName(organizationCode));
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

    default Course findByCourseName(MongoTemplate mongoTemplate, String organizationCode, String course) {
        Query query = new Query(Criteria.where("courseName").is(course));
        return mongoTemplate.findOne(query, Course.class, getCollectionName(organizationCode));
    }
}
