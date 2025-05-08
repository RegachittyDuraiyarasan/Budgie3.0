package com.hepl.budgie.service.impl.attendancemanagement;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.attendancemanagement.AttendanceDayTypeHistoryDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayType;
import com.hepl.budgie.entity.attendancemanagement.AttendanceDayTypeHistory;
import com.hepl.budgie.repository.attendancemanagement.AttendanceDayTypeHistoryRepository;
import com.hepl.budgie.repository.attendancemanagement.AttendanceDayTypeRepository;
import com.hepl.budgie.service.attendancemanagement.AttendanceDayTypeHistoryService;
import com.mongodb.bulk.BulkWriteResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class AttendanceDayTypeHistoryServiceImpl implements AttendanceDayTypeHistoryService {

    private final AttendanceDayTypeHistoryRepository attendanceDayTypeHistoryRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final AttendanceDayTypeRepository attendanceDayTypeRepository;

    @Override
    public AttendanceDayTypeHistory saveAttendanceDayType(AttendanceDayTypeHistoryDTO dayTypeHistory) {

        log.info("save attendance day type policy");
        String orgId = jwtHelper.getOrganizationCode();

        AttendanceDayType dayType = attendanceDayTypeRepository.findByIdAndOrg(dayTypeHistory.getDayType(), orgId,
                mongoTemplate);

        return attendanceDayTypeHistoryRepository.saveAttendanceDayType(orgId, dayTypeHistory, dayType, mongoTemplate);

    }

    @Override
    public List<AttendanceDayTypeHistory> getAttendanceDayTypeHistory(String empId, String monthYear) {

        log.info("fetch all attendance day type history");
        String orgId = jwtHelper.getOrganizationCode();
        String collection = attendanceDayTypeHistoryRepository.getCollectionName(orgId);

        Query query = new Query();
        if (empId != null && !empId.isEmpty()) {
            query.addCriteria(Criteria.where("empId").is(empId));
        }
        if (monthYear != null && !monthYear.isEmpty()) {
            query.addCriteria(Criteria.where("monthYear").is(monthYear));
        }
        return mongoTemplate.find(query,AttendanceDayTypeHistory.class, collection);
    }

    @Override
    public List<AttendanceDayTypeHistory> getByEmpId(String empId) {

        log.info("fetch all attendance day type history");
        String orgId = jwtHelper.getOrganizationCode();
        return attendanceDayTypeHistoryRepository.findByEmpId(empId, orgId, mongoTemplate);

    }

    @Override
    public BulkWriteResult excelImports(List<Map<String, Object>> validRows) {

        if (validRows.isEmpty()) {
            return BulkWriteResult.unacknowledged();
        }
        String orgId = jwtHelper.getOrganizationCode();
        return attendanceDayTypeHistoryRepository.dayTypeBulkUpsert(mongoTemplate,
                orgId,
                validRows);
    }
}
