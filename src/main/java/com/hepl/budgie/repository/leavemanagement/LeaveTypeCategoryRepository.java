package com.hepl.budgie.repository.leavemanagement;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.leavemanagement.LeaveTypeCategory;
import com.mongodb.client.result.UpdateResult;

import io.micrometer.common.util.StringUtils;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public interface LeaveTypeCategoryRepository extends MongoRepository<LeaveTypeCategory, String> {

	public static final String COLLECTION_NAME = "leave_class_type";

	default String getCollectionName(String org) {
		return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
	}

	default List<LeaveTypeCategory> findAllByActiveStatus(String org, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
		return mongoTemplate.find(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default LeaveTypeCategory save(LeaveTypeCategory entity, String org, MongoTemplate mongoTemplate) {
		return mongoTemplate.save(entity, getCollectionName(org));
	}

	default UpdateResult deleteLeaveTypeCategory(String id, String org, MongoTemplate mongoTemplate) {
		Query query = new Query(new Criteria().and("_id").is(id));

		Update update = new Update();
		update.set("status", Status.DELETED.label);

		return mongoTemplate.updateFirst(query, update, LeaveTypeCategory.class, getCollectionName(org));
	}

	default Optional<LeaveTypeCategory> findLatestLeaveType(String org, MongoTemplate mongoTemplate) {

		Query query = new Query().with(Sort.by(Sort.Direction.DESC, "leaveUniqueCode")).limit(1);

		return Optional.ofNullable(mongoTemplate.findOne(query, LeaveTypeCategory.class, getCollectionName(org)));
	}

	default Optional<LeaveTypeCategory> findByLeaveTypeCategoryId(String id, String org, MongoTemplate mongoTemplate) {
		return Optional.ofNullable(mongoTemplate.findById(id, LeaveTypeCategory.class, getCollectionName(org)));
	}

	default Map<String, String> fetchLeaveTypeCodeMap(String org, MongoTemplate mongoTemplate) {
		return findAllByActiveStatus(org, mongoTemplate).stream()
				.collect(Collectors.toMap(LeaveTypeCategory::getLeaveTypeName, LeaveTypeCategory::getLeaveTypeCode,
						(existing, replacement) -> existing));
	}

	default LeaveTypeCategory findByLeaveSchemeName(String schemeName, String org, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("leaveSchemeId.schemeName").is(schemeName).and("status").is(Status.ACTIVE.label));

		return mongoTemplate.findOne(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default List<LeaveTypeCategory> findBySchemeName(String schemeName, String org, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("leaveSchemeId.schemeName").is(schemeName));
		return mongoTemplate.find(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default LeaveTypeCategory findByLeaveTypeName(String leaveTypeName, String org, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("leaveTypeName").is(leaveTypeName).and("status").is(Status.ACTIVE.label));
		return mongoTemplate.findOne(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default LeaveTypeCategory findByLeaveSchemeAndLeaveTypeName(String schemeName, String leaveTypeName, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("leaveSchemeId.schemeName").is(schemeName).and("leaveTypeName")
				.is(leaveTypeName).and("status").is(Status.ACTIVE.label));
		return mongoTemplate.findOne(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default List<LeaveTypeCategory> findByBalanceDeduction(String balanceDeduction, String org,
			MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("balanceDeduction").is(balanceDeduction).and("status").is(Status.ACTIVE.label));
		return mongoTemplate.find(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default List<LeaveTypeCategory> findBySchemeNameAndBalanceDeduction(String schemeName, String balanceDeduction,
			String org, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("leaveSchemeId.schemeName").is(schemeName).and("balanceDeduction").is(balanceDeduction)
				.and("status").is(Status.ACTIVE.label));
		return mongoTemplate.find(query, LeaveTypeCategory.class, getCollectionName(org));
	}

	default Map<String, Integer> getLeaveTypePeriodicityDays(String organizationCode, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.fields().include("leaveTypeName", "periodicityDays");

		return mongoTemplate.find(query, LeaveTypeCategory.class, getCollectionName(organizationCode)).stream()
				.collect(Collectors.toMap(LeaveTypeCategory::getLeaveTypeName,
						lt -> lt.getPeriodicityDays() != null ? lt.getPeriodicityDays() : 0));
	}

	default Map<String, Set<String>> getSchemeLeaveTypeMapping(String organizationCode, MongoTemplate mongoTemplate) {
		try {
			Query query = new Query(Criteria.where("status").is(Status.ACTIVE.label));
			query.fields().include("leaveTypeCode").include("leaveTypeName").include("leaveSchemeId.schemeName");

			List<LeaveTypeCategory> leaveTypeCategories = mongoTemplate.find(query, LeaveTypeCategory.class,
					getCollectionName(organizationCode));

			Map<String, Set<String>> schemeLeaveTypes = new HashMap<>();

			for (LeaveTypeCategory category : leaveTypeCategories) {
				if (CollectionUtils.isEmpty(category.getLeaveSchemeId())) {
					continue;
				}

				String leaveTypeIdentifier = StringUtils.isNotBlank(category.getLeaveTypeCode())
						? category.getLeaveTypeCode()
						: category.getLeaveTypeName();

				for (LeaveTypeCategory.LeaveScheme scheme : category.getLeaveSchemeId()) {
					if (StringUtils.isNotBlank(scheme.getSchemeName())) {
						schemeLeaveTypes.computeIfAbsent(scheme.getSchemeName(), k -> new HashSet<>())
								.add(leaveTypeIdentifier);
					}
				}
			}

			return schemeLeaveTypes;
		} catch (Exception e) {
			return Collections.emptyMap();
		}
	}
}
