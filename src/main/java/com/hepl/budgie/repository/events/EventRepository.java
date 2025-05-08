package com.hepl.budgie.repository.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.hepl.budgie.entity.event.Event;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;

public interface EventRepository extends MongoRepository<Event, String> {
    
    public static final String COLLECTION_NAME = "m_events";
    default String getCollectionName(String org) {
        return org.isEmpty() ? COLLECTION_NAME : (COLLECTION_NAME + '_' + org);
    }
    default List<UserInfo> getAttendees(String org, String key, MongoTemplate mongoTemplate) {

        Query query = new Query();
        query.addCriteria(Criteria.where("subOrganization.organizationCode").in(org));
    
        if (key != null && !key.isEmpty() && !key.equalsIgnoreCase("all")) {
            query.addCriteria(new Criteria().orOperator(
                Criteria.where("sections.workingInformation.department").is(key),
                Criteria.where("sections.workingInformation.workLocation").is(key),
                Criteria.where("sections.workingInformation.designation").is(key),
                Criteria.where("sections.basicDetails.gender").is(key)
            ));
        }
    
        return mongoTemplate.find(query, UserInfo.class);
    }
    default List<Map<String, Object>> findMatchingEvents(String designation, String department, String workLocation,
        String gender, MongoTemplate mongoTemplate, String org,
        String empId, String month, UserInfoRepository userInfoRepository, String year) {

        String collection = getCollectionName(org);
        Criteria baseCriteria = Criteria.where("status").is("Active");

        Criteria dateCriteria;
        if (year != null && !year.isEmpty()) {
            int parsedYear = Integer.parseInt(year);
            LocalDateTime startOfYear = LocalDate.of(parsedYear, 1, 1).atStartOfDay();
            LocalDateTime endOfYear = LocalDate.of(parsedYear, 12, 31).atTime(LocalTime.MAX);
            dateCriteria = Criteria.where("startDate").gte(startOfYear).lte(endOfYear);
        } else {
            YearMonth yearMonth = YearMonth.parse(month); 
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
            dateCriteria = Criteria.where("startDate").gte(startOfMonth).lte(endOfMonth);
        }

        Criteria matchCriteria = new Criteria().orOperator(
        Criteria.where("allCandidate").is("yes"),

        new Criteria().andOperator(
        Criteria.where("filterType").is("Department"),
        Criteria.where("filterValue").is(department),
        Criteria.where("filterAll").is(true)
        ),

        new Criteria().andOperator(
        Criteria.where("filterType").is("Designation"),
        Criteria.where("filterValue").is(designation),
        Criteria.where("filterAll").is(true)
        ),

        new Criteria().andOperator(
        Criteria.where("filterType").is("Gender"),
        Criteria.where("filterValue").is(gender),
        Criteria.where("filterAll").is(true)
        ),

        new Criteria().andOperator(
        Criteria.where("filterType").is("WorkLocation"),
        Criteria.where("filterValue").is(workLocation),
        Criteria.where("filterAll").is(true)
        ),

        new Criteria().andOperator(
        Criteria.where("filterAll").is(false),
        Criteria.where("empIds").in(empId)
        )
        );

        MatchOperation match = Aggregation.match(new Criteria().andOperator(baseCriteria, matchCriteria, dateCriteria));
        Aggregation aggregation = Aggregation.newAggregation(match);

        List<Event> events = mongoTemplate.aggregate(aggregation, collection, Event.class).getMappedResults();
        List<Map<String, Object>> finalResult = new ArrayList<>();

        for (Event event : events) {

            Map<String, Object> eventMap = new HashMap<>();
            eventMap.put("id", event.getId());
            eventMap.put("eventName", event.getEventName());
            eventMap.put("where", event.getWhere());
            eventMap.put("description", event.getDescription());
            eventMap.put("eventFile", event.getEventFile());
            eventMap.put("color", event.getColor());
            eventMap.put("startTime", event.getStartTime());
            eventMap.put("endTime", event.getEndTime());
            eventMap.put("category", event.getCategory());
            eventMap.put("eventType", event.getEventType());
            eventMap.put("startDate", event.getStartDate());
            eventMap.put("endDate", event.getEndDate());
            eventMap.put("status", event.getStatus());
            // eventMap.put("empIds", event.getEmpIds());
            eventMap.put("allCandidate", event.getAllCandidate());
            eventMap.put("filterType", event.getFilterType());
            eventMap.put("filterValue", event.getFilterValue());
            eventMap.put("filterAll", event.isFilterAll());

            List<String> empIds = event.getEmpIds();
            if (empIds != null && !empIds.isEmpty()) {
                List<Map<String, String>> empDetails = new ArrayList<>();
                for (String emId : empIds) {
                    userInfoRepository.findByEmpId(emId).ifPresent(info -> {
                        Map<String, String> detail = new HashMap<>();
                        detail.put("empId", emId);
                        detail.put("name", info.getSections().getBasicDetails().getFirstName() + " " + info.getSections().getBasicDetails().getLastName());
                        detail.put("department", info.getSections().getWorkingInformation().getDepartment());
                        detail.put("designation", info.getSections().getWorkingInformation().getDesignation());
                        detail.put("gender", info.getSections().getBasicDetails().getGender());
                        detail.put("workLocation", info.getSections().getWorkingInformation().getWorkLocation());
                        empDetails.add(detail);
                    });
                }
                eventMap.put("empDetails", empDetails);
            } else {
                eventMap.put("empDetails", Collections.emptyList());
            }

            finalResult.add(eventMap);
        }

        return finalResult;
    }

    default List<Map<String, Object>> findAllEvents(String collectionName, String employee, String category, String eventType,
                                                MongoTemplate mongoTemplate, UserInfoRepository userInfoRepository, String month) {
            Query query = new Query();
            List<Criteria> criteriaList = new ArrayList<>();

            if (category != null) {
                criteriaList.add(Criteria.where("category").is(category));
            }

            if (eventType != null) {
                criteriaList.add(Criteria.where("eventType").is(eventType));
            }

            criteriaList.add(Criteria.where("status").is("Active"));

            if (month != null) {
                YearMonth yearMonth = YearMonth.parse(month); // e.g. "2025-04"
                LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
                LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);
                criteriaList.add(Criteria.where("startDate").gte(startOfMonth).lte(endOfMonth));
            }

            if (employee != null) {
                Optional<UserInfo> optionalUser = userInfoRepository.findByEmpId(employee);
                if (optionalUser.isPresent()) {
                    UserInfo user = optionalUser.get();

                    String department = user.getSections().getWorkingInformation().getDepartment();
                    String designation = user.getSections().getWorkingInformation().getDesignation();
                    String gender = user.getSections().getBasicDetails().getGender();
                    String workLocation = user.getSections().getWorkingInformation().getWorkLocation();

                    List<Criteria> matchConditions = new ArrayList<>();
                    matchConditions.add(Criteria.where("allCandidate").is("yes"));

                    matchConditions.add(new Criteria().andOperator(
                        Criteria.where("filterType").is("Department"),
                        Criteria.where("filterValue").is(department),
                        Criteria.where("filterAll").is(true)
                    ));

                    matchConditions.add(new Criteria().andOperator(
                        Criteria.where("filterType").is("Designation"),
                        Criteria.where("filterValue").is(designation),
                        Criteria.where("filterAll").is(true)
                    ));

                    matchConditions.add(new Criteria().andOperator(
                        Criteria.where("filterType").is("Gender"),
                        Criteria.where("filterValue").is(gender),
                        Criteria.where("filterAll").is(true)
                    ));

                    matchConditions.add(new Criteria().andOperator(
                        Criteria.where("filterType").is("WorkLocation"),
                        Criteria.where("filterValue").is(workLocation),
                        Criteria.where("filterAll").is(true)
                    ));

                    matchConditions.add(new Criteria().andOperator(
                        Criteria.where("filterAll").is(false),
                        Criteria.where("empIds").in(employee)
                    ));

                    criteriaList.add(new Criteria().orOperator(matchConditions.toArray(new Criteria[0])));
                }
            }

            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
            List<Event> events = mongoTemplate.find(query, Event.class, collectionName);

            List<Map<String, Object>> finalResult = new ArrayList<>();

            for (Event event : events) {

                Map<String, Object> eventMap = new HashMap<>();
                eventMap.put("id", event.getId());
                eventMap.put("eventName", event.getEventName());
                eventMap.put("where", event.getWhere());
                eventMap.put("description", event.getDescription());
                eventMap.put("eventFile", event.getEventFile());
                eventMap.put("color", event.getColor());
                eventMap.put("startTime", event.getStartTime());
                eventMap.put("endTime", event.getEndTime());
                eventMap.put("category", event.getCategory());
                eventMap.put("eventType", event.getEventType());
                eventMap.put("startDate", event.getStartDate());
                eventMap.put("endDate", event.getEndDate());
                eventMap.put("status", event.getStatus());
                // eventMap.put("empIds", event.getEmpIds());
                eventMap.put("allCandidate", event.getAllCandidate());
                eventMap.put("filterType", event.getFilterType());
                eventMap.put("filterValue", event.getFilterValue());
                eventMap.put("filterAll", event.isFilterAll());

                List<String> empIds = event.getEmpIds();
                if (empIds != null && !empIds.isEmpty()) {
                    List<Map<String, String>> empDetails = new ArrayList<>();
                    for (String empId : empIds) {
                        userInfoRepository.findByEmpId(empId).ifPresent(info -> {
                            Map<String, String> detail = new HashMap<>();
                            detail.put("empId", empId);
                            detail.put("name", info.getSections().getBasicDetails().getFirstName() + " " + info.getSections().getBasicDetails().getLastName());
                            detail.put("department", info.getSections().getWorkingInformation().getDepartment());
                            detail.put("designation", info.getSections().getWorkingInformation().getDesignation());
                            detail.put("gender", info.getSections().getBasicDetails().getGender());
                            detail.put("workLocation", info.getSections().getWorkingInformation().getWorkLocation());
                            empDetails.add(detail);
                        });
                    }
                    eventMap.put("empDetails", empDetails);
                } else {
                    eventMap.put("empDetails", Collections.emptyList());
                }

                finalResult.add(eventMap);
            }

            return finalResult;
        }


}
