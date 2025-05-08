package com.hepl.budgie.repository.userinfo;

import com.hepl.budgie.dto.employee.EmployeeActiveDTO;
import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.event.WishesDTO;
import com.hepl.budgie.dto.movement.EmpCodeValueDTO;
import com.hepl.budgie.dto.payroll.PayrollPfListDTO;
import com.hepl.budgie.dto.separation.EmployeeInfoDTO;
import com.hepl.budgie.dto.userlogin.AuthSwitch;
import com.hepl.budgie.dto.userlogin.UserSwitchAuth;
import com.hepl.budgie.dto.userinfo.DivisionHeadDTO;
import com.hepl.budgie.dto.userinfo.HRInfoDTO;
import com.hepl.budgie.dto.userinfo.PrimaryDTO;
import com.hepl.budgie.dto.userinfo.ReviewerDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.event.Wishes;
import com.hepl.budgie.entity.event.WishesType;
import com.hepl.budgie.entity.separation.SeparationInfo;
import com.hepl.budgie.entity.userinfo.HrInformation;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.entity.userinfo.WorkingInformation;
import com.hepl.budgie.utils.MongoExpressionHelper;

import com.hepl.budgie.utils.AppMessages;
import org.bson.Document;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import java.util.TimeZone;
import java.util.Set;
import java.util.stream.Collectors;

public interface UserInfoRepository extends MongoRepository<UserInfo, String> {

	public static final String COLLECTION_NAME = "userinfo";

	Page<UserInfo> findAll(Pageable pageable);

	Optional<UserInfo> findByEmpId(String empId);

	<T> Optional<T> findByEmpId(String empId, Class<T> projectionClass);

	Optional<UserInfo> findByEmpIdAndStatus(String empId, String status);

	List<UserInfo> findByStatus(String status);

	default List<UserInfo> findByEmpIdInAndStatus(List<String> empIds, String status, MongoTemplate mongoTemplate) {

		Query query = new Query(Criteria.where("empId").in(empIds).and("status").is(status));

		return mongoTemplate.find(query, UserInfo.class, COLLECTION_NAME);
	}

	boolean existsByTempId(String tempId);

	default Map<String, String> fetchEmployeeLeaveSchemes(MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is("Active"));

		query.fields().include("empId").include("sections.hrInformation.leaveScheme");

		List<UserInfo> activeEmployees = mongoTemplate.find(query, UserInfo.class);

		return activeEmployees.stream().filter(emp -> emp.getSections().getHrInformation().getLeaveScheme() != null)
				.collect(Collectors.toMap(UserInfo::getEmpId,
						emp -> emp.getSections().getHrInformation().getLeaveScheme()));
	}

	default Map<String, String> fetchEmployeeLeaveSchemes(List<String> empIds, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(Criteria.where("status").is("Active").and("empId").in(empIds));
		query.fields().include("empId").include("sections.hrInformation.leaveScheme");

		List<UserInfo> employees = mongoTemplate.find(query, UserInfo.class);

		return employees.stream()
				.filter(emp -> emp.getSections() != null && emp.getSections().getHrInformation() != null
						&& emp.getSections().getHrInformation().getLeaveScheme() != null)
				.collect(Collectors.toMap(UserInfo::getEmpId,
						emp -> emp.getSections().getHrInformation().getLeaveScheme()));
	}

	default List<UserInfo> findByLeaveSchemeAndStatus(String leaveScheme, String status, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("sections.hrInformation.leaveScheme").is(leaveScheme).and("status").is(status));

		return mongoTemplate.find(query, UserInfo.class, COLLECTION_NAME);
	}

	default UserInfo findByEmpIdAndIsProbation(String empId, boolean isProbation, MongoTemplate mongoTemplate) {
		Query query = new Query();
		query.addCriteria(
				Criteria.where("empId").is(empId).and("sections.probationDetails.isProbation").is(isProbation));

		query.fields().include("empId").include("sections.probationDetails");

		return mongoTemplate.findOne(query, UserInfo.class, COLLECTION_NAME);
	}

	default AggregationResults<PrimaryDTO> listAllPrimaryManagers(MongoTemplate mongoTemplate) {

		MatchOperation matchOperation = new MatchOperation(
				Criteria.where("sections.hrInformation.primary.managerId").exists(true));
		LookupOperation lookupOperation = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.primary.managerId").foreignField("empId").as("managerDetails");
		UnwindOperation unwindOperation = Aggregation.unwind("managerDetails");
		ProjectionOperation operation = Aggregation.project().and("sections.hrInformation.primary.managerId")
				.as("managerId")
				.andExpression(
						"concat(managerDetails.sections.basicDetails.firstName,' ',managerDetails.sections.basicDetails.lastName)")
				.as("empName");
		GroupOperation groupOperation = Aggregation.group("managerId").first("managerId").as("managerId")
				.addToSet("empName").as("empName");

		Aggregation aggregation = Aggregation.newAggregation(matchOperation, lookupOperation, unwindOperation,
				operation, groupOperation);

		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, PrimaryDTO.class);
	}

	default AggregationResults<ReviewerDTO> listAllReviewer(MongoTemplate mongoTemplate) {

		MatchOperation matchOperation = new MatchOperation(
				Criteria.where("sections.hrInformation.reviewer.managerId").exists(true));
		LookupOperation lookupOperation = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.reviewer.managerId").foreignField("empId").as("reviewerDetails");
		UnwindOperation unwindOperation = Aggregation.unwind("reviewerDetails");
		ProjectionOperation operation = Aggregation.project().and("sections.hrInformation.reviewer.managerId")
				.as("managerId")
				.andExpression(
						"concat(reviewerDetails.sections.basicDetails.firstName,' ',reviewerDetails.sections.basicDetails.lastName)")
				.as("empName");
		GroupOperation groupOperation = Aggregation.group("managerId").first("managerId").as("managerId")
				.addToSet("empName").as("empName");

		Aggregation aggregation = Aggregation.newAggregation(matchOperation, lookupOperation, unwindOperation,
				operation, groupOperation);

		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, ReviewerDTO.class);
	}

	default AggregationResults<DivisionHeadDTO> listAllDivisionHead(MongoTemplate mongoTemplate) {

		MatchOperation matchOperation = new MatchOperation(
				Criteria.where("sections.hrInformation.divisionHead.managerId").exists(true));
		LookupOperation lookupOperation = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.divisionHead.managerId").foreignField("empId")
				.as("divisionHeadDetails");
		UnwindOperation unwindOperation = Aggregation.unwind("divisionHeadDetails");
		ProjectionOperation operation = Aggregation.project().and("sections.hrInformation.divisionHead.managerId")
				.as("managerId")
				.andExpression(
						"concat(divisionHeadDetails.sections.basicDetails.firstName,' ',divisionHeadDetails.sections.basicDetails.lastName)")
				.as("empName");
		GroupOperation groupOperation = Aggregation.group("managerId").first("managerId").as("managerId")
				.addToSet("empName").as("empName");

		Aggregation aggregation = Aggregation.newAggregation(matchOperation, lookupOperation, unwindOperation,
				operation, groupOperation);

		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, DivisionHeadDTO.class);
	}

	List<UserInfo> findByStatus(boolean status);

	boolean existsByEmpId(String newEmpId);

	default List<EmpCodeValueDTO> fetchEmployeeUnderRM(String managerId) {
		return findAll().stream().filter(user -> {
			HrInformation hrInfo = user.getSections().getHrInformation();
			return hrInfo != null && hrInfo.getPrimary() != null
					&& managerId.equals(hrInfo.getPrimary().getManagerId());
		}).map(user -> new EmpCodeValueDTO(
				user.getSections().getBasicDetails().getFirstName() + " "
						+ user.getSections().getBasicDetails().getLastName() + " - " + user.getEmpId(),
				user.getEmpId())).toList();
	}

	List<UserInfo> findAllByEmpId(String authenticatedEmpId);

	List<UserInfo> findByEmpIdIn(List<String> managerIds);

	boolean existsByEmpIdAndStatus(String empId, boolean status);

	default List<UserInfo> findEmpIdsUnderReviewer(String authenticatedEmpId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("sections.hrInformation.reviewer.managerId").is(authenticatedEmpId));
		return mongoTemplate.find(query, UserInfo.class, COLLECTION_NAME);
	}

	default Optional<UserInfo> getUsersByEmpIdBySwitchAuth(UserSwitchAuth authSwitch, String empId,
			MongoTemplate mongoTemplate, String activeOrg) {
		Query query = null;
		if (authSwitch.getType().equals(AuthSwitch.ORGANIZATION.label)) {
			query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
					Criteria.where("subOrganization.organizationCode").is(authSwitch.getValue())));
		} else {
			query = new Query(new Criteria().andOperator(Criteria.where("empId").is(empId),
					Criteria.where("subOrganization.organizationCode").is(activeOrg),
					Criteria.where("subOrganization.roleDetails").is(authSwitch.getValue())));
		}

		return Optional.ofNullable(mongoTemplate.findOne(query, UserInfo.class));
	}

	default boolean getReviewerStatus(String empId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("sections.hrInformation.reviewer.managerId").is(empId));

		return mongoTemplate.count(query, COLLECTION_NAME) > 0;
	}

	default boolean getReportingManagerStatus(String empId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("sections.hrInformation.primary.managerId").is(empId));

		return mongoTemplate.count(query, COLLECTION_NAME) > 0;
	}

	default void updateSpocStatus(String empId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId));

		Update update = new Update();
		update.set("sections.hrInformation.spocStatus", true);

		Optional<UserInfo> user = Optional.ofNullable(mongoTemplate.findAndModify(query, update, UserInfo.class));
		if (user.isPresent()) {
			updateSpocReportingManagerStatus(user.get().getSections().getHrInformation().getPrimary().getManagerId(),
					mongoTemplate);
		}

	}

	default void updateSpocReportingManagerStatus(String empId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId));

		Update update = new Update();
		update.set("sections.hrInformation.spocReportingManagerStatus", true);

		mongoTemplate.updateFirst(query, update, UserInfo.class);
	}

	default Set<String> findActiveEmployeeIds(MongoTemplate mongoTemplate, String orgId) {
		Query query = new Query(
				Criteria.where("status").is(Status.ACTIVE.label).and("organization.organizationCode").is(orgId));
		query.fields().include("empId").exclude("_id");

		return mongoTemplate.find(query, UserInfo.class, COLLECTION_NAME).stream().map(UserInfo::getEmpId)
				.collect(Collectors.toSet());
	}

	default List<EmployeeActiveDTO> getEmployeeDetailsIfReportee(String orgCode, List<String> employeeId,
			MongoTemplate mongoTemplate) {
		MatchOperation matchOperation = Aggregation.match(
				new Criteria().andOperator(Criteria.where("sections.hrInformation.primary.managerId").in(employeeId),
						Criteria.where("subOrganization.organizationCode").in(orgCode)));

		StringOperators.Concat concat = StringOperators.Concat
				.valueOf("reportingManager.sections.basicDetails.firstName").concat(" ")
				.concatValueOf("reportingManager.sections.basicDetails.lastName");
		ProjectionOperation projectionOperation = Aggregation.project().and("reportingManager.empId").as("empId")
				.and(concat).as("employeeName");

		GroupOperation groupOperation = Aggregation.group("empId").first("empId").as("empId").first("employeeName")
				.as("employeeName");
		Aggregation aggregation = Aggregation.newAggregation(matchOperation,
				lookupEmployeeDetails("sections.hrInformation.primary.managerId", "reportingManager"),
				Aggregation.unwind("reportingManager"), projectionOperation, groupOperation);
		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, EmployeeActiveDTO.class).getMappedResults();

	}

	default AggregationResults<EmployeeOrgChartDTO> getTeamListByEmpId(String empId, String orgCode, TimeZone timezone,
			MongoTemplate mongoTemplate) {

		MatchOperation matchOperation = Aggregation
				.match(new Criteria().andOperator(Criteria.where("sections.hrInformation.primary.managerId").is(empId),
						Criteria.where("subOrganization.organizationCode").is(orgCode)));

		ProjectionOperation projectionOperation = getEmployeeOrgChart(timezone, "")
				.and(MongoExpressionHelper.dateDiff("$$NOW", "sections.workingInformation.doj", "year")).as("years")
				.and(MongoExpressionHelper.dateDiff("$$NOW", "sections.workingInformation.doj", "month")).as("months");

		Aggregation aggregation = Aggregation.newAggregation(matchOperation, projectionOperation);

		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, EmployeeOrgChartDTO.class);
	}

	default ProjectionOperation getEmployeeOrgChart(TimeZone timezone, String year) {
		StringOperators.Concat concat = StringOperators.Concat.valueOf("sections.basicDetails.firstName").concat(" ")
				.concatValueOf("sections.basicDetails.lastName");
		ProjectionOperation projectionOperation = Aggregation.project().and("empId").as("empId").and(concat).as("name")
				.and("sections.workingInformation.designation").as("designation")
				.and("sections.workingInformation.department").as("department")
				.and("sections.workingInformation.officialEmail").as("email")
				.and("sections.contact.primaryContactNumber").as("primaryContactNumber")
				.and("sections.workingInformation.workLocation").as("workLocation").and("sections.profilePicture")
				.as("profile").and("sections.bannerImage").as("banner")
				.and(MongoExpressionHelper.dateToString("sections.workingInformation.doj", timezone))
				.as("dateOfJoining").and(MongoExpressionHelper.dateToString("sections.basicDetails.dob", timezone))
				.as("dateOfBirth");
		if (!year.isEmpty()) {
			projectionOperation = projectionOperation
					.and(StringOperators.Concat.stringValue(year).concatValueOf(
							MongoExpressionHelper.dateToString("sections.workingInformation.doj", "-%m-%d", timezone)))
					.as("currentDateOfJoining")
					.and(StringOperators.Concat.stringValue(year).concatValueOf(
							MongoExpressionHelper.dateToString("sections.basicDetails.dob", "-%m-%d", timezone)))
					.as("currentDateOfBirth");
		}
		return projectionOperation;
	}

	private LookupOperation lookupEmployeeDetails(String localField, String alias) {
		return LookupOperation.newLookup().from(COLLECTION_NAME).localField(localField).foreignField("empId").as(alias);
	}

	default List<UserInfo> getEmployeesByCriteria(String search, String field, MongoTemplate mongoTemplate) {
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where(field).is(search)));

		AggregationResults<UserInfo> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME, UserInfo.class);
		return results.getMappedResults();
	}

	private LookupOperation lookupSeparationDetails(String localField, String alias, String orgCode) {
		return LookupOperation.newLookup().from("separationInfo_" + orgCode).localField(localField)
				.foreignField("empId").as(alias);
	}

	default EmployeeInfoDTO getEmployeeDetails(String empId, String orgCode, MongoTemplate mongoTemplate,
			TimeZone timezone) {
		MatchOperation matchStage = Aggregation.match(Criteria.where("empId").is(empId));

		LookupOperation lookupReportingManager = lookupEmployeeDetails("sections.hrInformation.primary.managerId",
				"reportingManagerDetails");

		LookupOperation lookupReviewer = lookupEmployeeDetails("sections.hrInformation.reviewer.managerId",
				"reviewerDetails");

		LookupOperation lookupSeparation = lookupSeparationDetails("empId", "separationDetails", orgCode);

		AddFieldsOperation filterPendingSeparation = Aggregation.addFields().addField("separationDetails")
				.withValue(ArrayOperators.Filter.filter("separationDetails").as("detail").by(BooleanOperators.Or.or(
						ComparisonOperators.Eq.valueOf("$$detail.resignationStatus").equalToValue("Pending"),
						ComparisonOperators.Eq.valueOf("$$detail.resignationStatus").equalToValue("Completed"))))
				.build();

		AddFieldsOperation extractLatestSeparation = Aggregation.addFields().addField("latestSeparationDetail")
				.withValue(ArrayOperators.ArrayElemAt.arrayOf("separationDetails").elementAt(-1)).build();

		ProjectionOperation projectStage = Aggregation.project().and("_id").as("id").and("empId").as("empId")
				.and("sections.basicDetails.firstName").as("firstName").and("sections.basicDetails.middleName")
				.as("middleName").and("sections.basicDetails.lastName").as("lastName")
				.and("sections.workingInformation.designation").as("designation")
				.and("sections.workingInformation.department").as("department")
				.and("sections.hrInformation.noticePeriod").as("noticePeriod")
				.and(MongoExpressionHelper.dateToString("$sections.workingInformation.doj", "%Y-%m-%d", timezone))
				.as("dateOfJoining").and("sections.hrInformation.primary.managerId").as("reportingManager")
				.and("sections.hrInformation.reviewer.managerId").as("reviewer").and("sections.contact.personalEmailId")
				.as("personalEmail").and("sections.workingInformation.officialEmail").as("officalEmail")
				.and("sections.contact.primaryContactNumber").as("contactNumber")
				.and("sections.accountInformation.pfNo").as("pfNo").and("sections.accountInformation.uanNo").as("uanNo")
				.and(ArrayOperators.Filter
						.filter(ConditionalOperators
								.ifNull("sections.accountInformation.bankDetails").then(Collections.emptyList()))
						.as("bankDetail").by(ComparisonOperators.Eq.valueOf("$$bankDetail.status").equalToValue(true)))
				.as("bankDetails")

				.andExpression("concat(arrayElemAt(reportingManagerDetails.sections.basicDetails.firstName, 0), ' ', "
						+ "arrayElemAt(reportingManagerDetails.sections.basicDetails.lastName, 0))")
				.as("reportingManagerName")
				.andExpression("concat(arrayElemAt(reviewerDetails.sections.basicDetails.firstName, 0), ' ', "
						+ "arrayElemAt(reviewerDetails.sections.basicDetails.lastName, 0))")
				.as("reviewerName")

				.and(ConditionalOperators.ifNull(
						MongoExpressionHelper.dateToString("$latestSeparationDetail.appliedDate", "%Y-%m-%d", timezone))
						.then("null"))
				.as("appliedDate")
				.and(ConditionalOperators.ifNull("$latestSeparationDetail.resignationStatus").then("null"))
				.as("resignationStatus")
				.and(ConditionalOperators.ifNull("$latestSeparationDetail.employeeRemarks").then("null")).as("remarks")
				.and(ConditionalOperators.ifNull("$latestSeparationDetail.reason").then("null")).as("reason")
				.and(ConditionalOperators.ifNull(MongoExpressionHelper
						.dateToString("$latestSeparationDetail.relievingDate", "%Y-%m-%d", timezone)).then("null"))
				.as("relievingDate");

		Aggregation aggregation = Aggregation.newAggregation(matchStage, lookupReportingManager, lookupReviewer,
				lookupSeparation, filterPendingSeparation, extractLatestSeparation, projectStage);

		AggregationResults<EmployeeInfoDTO> results = mongoTemplate.aggregate(aggregation, "userinfo",
				EmployeeInfoDTO.class);
		return results.getUniqueMappedResult();
	}

	default List<UserInfo> fetchUserDetails(String organizationCode, String empId, MongoTemplate mongoTemplate) {
		// String collectionName = COLLECTION_NAME + (organizationCode.isEmpty() ? "" :
		// "_" + organizationCode); // Dynamic collection based on org

		Query query = new Query();
		query.addCriteria(Criteria.where("sections.hrInformation.primary.managerId").is(empId));
		query.addCriteria(Criteria.where("sections.probationDetails.isProbation").is(true));

		List<UserInfo> probationUsers = mongoTemplate.find(query, UserInfo.class);

		if (probationUsers.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.PROBATION);
		}
		return probationUsers;
	}

	default AggregationResults<UserInfo> getOrganizationChart(String organizationCode, MongoTemplate mongoTemplate) {
		MatchOperation matchOperation = Aggregation
				.match(new Criteria().andOperator(Criteria.where("subOrganization.roleDetails").is("Business Head"),
						Criteria.where("subOrganization.organizationCode").is(organizationCode)));
		GraphLookupOperation graphLookupOperation = Aggregation.graphLookup(COLLECTION_NAME).startWith("empId")
				.connectFrom("empId").connectTo("sections.hrInformation.primary.managerId").as("children");

		Aggregation aggregation = Aggregation.newAggregation(matchOperation, graphLookupOperation);
		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, UserInfo.class);
	}

	default AggregationResults<EmployeeOrgChartDTO> getEmployeeListBasedOnMatchDayAndMonth(WishesDTO wishesDTO,
			String organizationCode, String field, TimeZone timezone, MongoTemplate mongoTemplate, int limit,
			WishesType type, String fromEmpId, String wishType) {
		MatchOperation matchOperation = Aggregation
				.match(Criteria.where("sections.workingInformation.payrollStatus").is(organizationCode));
		BooleanOperators.And andOperator;
		if (type == WishesType.TODAY) {
			andOperator = BooleanOperators.And.and(
					ComparisonOperators.Eq.valueOf(MongoExpressionHelper.dateOperatorMonth(field, timezone))
							.equalToValue(wishesDTO.getMonth()),
					ComparisonOperators.Eq.valueOf(MongoExpressionHelper.dateOperatorDayOfMonth(field, timezone))
							.equalToValue(wishesDTO.getDate()));
		} else if (type == WishesType.MONTH) {
			andOperator = BooleanOperators.And
					.and(ComparisonOperators.Eq.valueOf(MongoExpressionHelper.dateOperatorMonth(field, timezone))
							.equalToValue(wishesDTO.getMonth()));
		} else {
			andOperator = BooleanOperators.And
					.and(ComparisonOperators.Eq.valueOf("empId").equalToValue(wishesDTO.getEmployee()));
		}

		MatchOperation matchDateOperation = Aggregation.match(Criteria.expr(andOperator));

		Aggregation aggregation = null;
		if (limit != 0) {
			LimitOperation limitOperation = Aggregation.limit(limit);
			aggregation = Aggregation.newAggregation(matchOperation, matchDateOperation,
					getEmployeeOrgChart(timezone, String.valueOf(wishesDTO.getYear())), limitOperation);
		} else {
			aggregation = Aggregation.newAggregation(matchOperation, matchDateOperation,
					getEmployeeOrgChart(timezone, String.valueOf(wishesDTO.getYear())));
		}
		AggregationResults<EmployeeOrgChartDTO> results = mongoTemplate.aggregate(aggregation, COLLECTION_NAME,
				EmployeeOrgChartDTO.class);
		List<EmployeeOrgChartDTO> employeeList = results.getMappedResults();

		List<String> toEmpIds = employeeList.stream().map(EmployeeOrgChartDTO::getEmpId).collect(Collectors.toList());

		LocalDate today = LocalDate.now(timezone.toZoneId());
		Date startOfDay = Date.from(today.atStartOfDay(timezone.toZoneId()).toInstant());
		Date endOfDay = Date.from(today.plusDays(1).atStartOfDay(timezone.toZoneId()).minusNanos(1).toInstant());

		Query wishQuery = new Query();
		wishQuery.addCriteria(Criteria.where("from").is(fromEmpId).and("to").in(toEmpIds).and("type").is(wishType)
				.and("time").gte(startOfDay).lte(endOfDay));

		String cln = "t_wishes_" + organizationCode;
		List<Wishes> sentWishes = mongoTemplate.find(wishQuery, Wishes.class, cln);

		Set<String> sentToEmpIds = sentWishes.stream().map(Wishes::getTo).collect(Collectors.toSet());

		for (EmployeeOrgChartDTO employee : employeeList) {
			employee.setMailSent(sentToEmpIds.contains(employee.getEmpId()));
		}

		return new AggregationResults<>(employeeList, results.getRawResults());
	}

	default org.bson.Document getEmployeeCurrentDetails(MongoTemplate mongoTemplate, String empId) {
		Query query = new Query();
		query.addCriteria(Criteria.where("empId").is(empId));

		query.fields().include("sections.hrInformation.primary.managerId")
				.include("sections.hrInformation.reviewer.managerId").include("sections.workingInformation.department")
				.include("sections.workingInformation.designation");

		return mongoTemplate.findOne(query, Document.class, COLLECTION_NAME);
	}

	default List<PayrollPfListDTO> findByPfEmployeeDetails(MongoTemplate mongoTemplate, String orgId) {
		Criteria criteria = Criteria.where("sections.workingInformation.payrollStatus").is(orgId);

		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(criteria),
				Aggregation.lookup("payroll_m_pf", "sections.workingInformation.applyPF", "pfId", "pf"), // Ensure
																											// correct
																											// collection
																											// name
				Aggregation.unwind("pf", true), // Unwind but keep documents even if there's no match
				Aggregation.project().and("empId").as("empId").and("sections.workingInformation.payrollState")
						.as("payrollState").and("sections.workingInformation.applyPF").as("pfLogic")
						.and(ConditionalOperators.ifNull("pf.pfName").then("")).as("pfName") // If no match, return
																								// empty string
						.andExpression("concat(ifNull(sections.basicDetails.firstName, ''), ' ', "
								+ "ifNull(sections.basicDetails.middleName, ''), ' ', "
								+ "ifNull(sections.basicDetails.lastName, ''))")
						.as("empName") // Employee full name
		);
		return mongoTemplate.aggregate(aggregation, COLLECTION_NAME, PayrollPfListDTO.class).getMappedResults();
	}

	default List<HRInfoDTO> getEmployeeManagerDetails(String empId, MongoTemplate mongoTemplate) {
		// Lookup for Primary Manager
		LookupOperation lookupPrimaryManager = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.primary.managerId").foreignField("empId").as("primaryManager");

		// Lookup for Reviewer
		LookupOperation lookupReviewer = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.reviewer.managerId").foreignField("empId").as("reviewer");

		// Lookup for Division Head
		LookupOperation lookupDivisionHead = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.divisionHead.managerId").foreignField("empId").as("divisionHead");

		// Lookup for Onboarder
		LookupOperation lookupOnboarder = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.onboarder.managerId").foreignField("empId").as("onboarder");

		// Lookup for Recruiter
		LookupOperation lookupRecruiter = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.recruiter.managerId").foreignField("empId").as("recruiter");

		// Lookup for Buddy
		LookupOperation lookupBuddy = LookupOperation.newLookup().from("userinfo")
				.localField("sections.hrInformation.buddy.managerId").foreignField("empId").as("buddy");

		// Aggregation Pipeline
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("empId").is(empId)),
				lookupPrimaryManager, lookupReviewer, lookupDivisionHead, lookupOnboarder, lookupRecruiter, lookupBuddy,
				Aggregation.project().and("empId").as("empId").and("sections.basicDetails.firstName").as("firstName")
						.and("sections.basicDetails.lastName").as("lastName")

						.and("sections.hrInformation.primary.managerId").as("primaryManagerId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$primaryManager.sections.basicDetails.firstName"))
						.as("primaryManagerName").and("primaryManager.sections.basicDetails.lastName")
						.as("primaryManagerLastName")

						.and("sections.hrInformation.reviewer.managerId").as("reviewerId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$reviewer.sections.basicDetails.firstName"))
						.as("reviewerName").and("reviewer.sections.basicDetails.lastName").as("reviewerLastName")

						.and("sections.hrInformation.divisionHead.managerId").as("divisionHeadId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$divisionHead.sections.basicDetails.firstName"))
						.as("divisionHeadName").and("divisionHead.sections.basicDetails.lastName")
						.as("divisionHeadLastName")

						.and("sections.hrInformation.onboarder.managerId").as("onboarderId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$onboarder.sections.basicDetails.firstName"))
						.as("onboarderName").and("onboarder.sections.basicDetails.lastName").as("onboarderLastName")

						.and("sections.hrInformation.recruiter.managerId").as("recruiterId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$recruiter.sections.basicDetails.firstName"))
						.as("recruiterName").and("recruiter.sections.basicDetails.lastName").as("recruiterLastName")

						.and("sections.hrInformation.buddy.managerId").as("buddyId")
						.and(ConditionalOperators.when(Criteria.where("roleDetails").in("Business Head"))
								.then("HEPL Supervisor").otherwise("$buddy.sections.basicDetails.firstName"))
						.as("buddyName").and("buddy.sections.basicDetails.lastName").as("buddyLastName")

		);
		AggregationResults<HRInfoDTO> results = mongoTemplate.aggregate(aggregation, "userinfo", HRInfoDTO.class);

		return results.getMappedResults();
	}

	default String findManagerIdByEmpId(String empId, MongoTemplate mongoTemplate) {
		Query query = new Query(Criteria.where("empId").is(empId));
		query.fields().include("sections.hrInformation.primary.managerId");

		UserInfo userInfo = mongoTemplate.findOne(query, UserInfo.class);
		return userInfo != null && userInfo.getSections() != null && userInfo.getSections().getHrInformation() != null
				&& userInfo.getSections().getHrInformation().getPrimary() != null
						? userInfo.getSections().getHrInformation().getPrimary().getManagerId()
						: null;
	}

	default boolean existsByOrganisationAndEmpId(MongoTemplate mongoTemplate, String organizationCode,
			String authUser) {
		Query query = new Query(Criteria.where("sections.workingInformation.payrollStatus").is(organizationCode)
				.and("empId").is(authUser));
		return mongoTemplate.exists(query, UserInfo.class);
	}

	default List<UserInfo> findReportingUsers(String empId, MongoTemplate mongoTemplate) {

		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(
				new Criteria().orOperator(Criteria.where("sections.hrInformation.primary.managerId").is(empId),
						Criteria.where("sections.hrInformation.secondary.managerId").is(empId),
						Criteria.where("sections.hrInformation.reviewer.managerId").is(empId)),
				Criteria.where("status").is("Active")));

		return mongoTemplate.find(query, UserInfo.class);
	}

	default WorkingInformation getWorkInfo(MongoTemplate mongoTemplate, String empId) {
		Aggregation aggregation = Aggregation.newAggregation(Aggregation.match(Criteria.where("empId").is(empId)),
				Aggregation.unwind("sections.workingInformation"), // Filter by empId
				Aggregation.project("sections.workingInformation").and("sections.workingInformation.doj").as("doj")// Select
																													// only
																													// workingInformation
						.and("sections.workingInformation.dateOfRelieving").as("dateOfRelieving") // Select only
																									// workingInformation

		);

		return mongoTemplate.aggregate(aggregation, "userinfo", WorkingInformation.class).getUniqueMappedResult();

	}

	default void updateUserInfoForUpcomingRelieving(List<SeparationInfo> separationInfos, MongoTemplate mongoTemplate) {
		List<String> empIds = separationInfos.stream().map(SeparationInfo::getEmpId).toList();

		if (empIds.isEmpty()) {
		}
		Query query = new Query(new Criteria().andOperator(Criteria.where("empId").in(empIds),
				Criteria.where("conditions.key").is("isExit")));
		Update update = new Update().set("conditions.$.value", true);

		mongoTemplate.updateMulti(query, update, UserInfo.class);
	}

	default List<UserInfo> findFilteredEmployeesForPayrollAdmin(String empId, String orgId, String reviewer,
			String repManager, String department, String designation, String payrollStatus, String location,
			MongoTemplate mongoTemplate) {
		List<Criteria> criteriaList = new ArrayList<>();
		criteriaList.add(Criteria.where("status").is("Active").and("organization.organizationCode").is(orgId));

		if (empId != null) {
			criteriaList.add(Criteria.where("empId").is(empId));
		}
		if (reviewer != null) {
			criteriaList.add(Criteria.where("sections.hrInformation.reviewer.managerId").is(reviewer));
		}
		if (repManager != null) {
			criteriaList.add(new Criteria().orOperator(
					Criteria.where("sections.hrInformation.reportingManager.primary.managerId").is(repManager),
					Criteria.where("sections.hrInformation.reportingManager.secondary.managerId").is(repManager)));
		}
		if (department != null) {
			criteriaList.add(Criteria.where("sections.workingInformation.department").is(department));
		}
		if (designation != null) {
			criteriaList.add(Criteria.where("sections.workingInformation.designation").is(designation));
		}
		if (payrollStatus != null) {
			criteriaList.add(Criteria.where("sections.workingInformation.payrollStatus").is(payrollStatus));
		}
		if (location != null) {
			criteriaList.add(Criteria.where("sections.workingInformation.workLocation").is(location));
		}
		Query query = new Query(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));

		return mongoTemplate.find(query, UserInfo.class);
	}

	default List<UserInfo> findUsersByRoleType(List<String> roleTypes, String orgId, MongoTemplate mongoTemplate,
			String orgId2) {

		Query query = new Query(Criteria.where("sections.workingInformation.payrollStatus").is(orgId)
				.and("sections.workingInformation.roleOfIntake").in(roleTypes).and("status").is("Active"));

		return mongoTemplate.find(query, UserInfo.class, COLLECTION_NAME);

	}

    default void updateMetro(String empId, MongoTemplate mongoTemplate, String metro){

		Query query = new Query(Criteria.where("empId").is(empId).and("status").is(Status.ACTIVE.label));
		Update update = new Update().set("payrollDetails.metro", metro);
		mongoTemplate.updateFirst(query, update, UserInfo.class);
	}
}
