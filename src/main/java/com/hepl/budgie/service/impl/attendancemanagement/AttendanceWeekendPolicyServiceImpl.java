package com.hepl.budgie.service.impl.attendancemanagement;

import java.util.*;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.attendancemanagement.AttendanceWeekendDTO;
import com.hepl.budgie.entity.attendancemanagement.AttendanceWeekendPolicy;
import com.hepl.budgie.entity.attendancemanagement.WeekEnd;
import com.hepl.budgie.repository.attendancemanagement.AttendanceWeekendPolicyRepository;
import com.hepl.budgie.service.attendancemanagement.AttendanceWeekendPolicyService;
import com.hepl.budgie.utils.AppMessages;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AttendanceWeekendPolicyServiceImpl implements AttendanceWeekendPolicyService {

    private final AttendanceWeekendPolicyRepository attendanceWeekendPolicyRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;

    @Override
    public AttendanceWeekendPolicy saveWeekendPolicy(AttendanceWeekendDTO weekend) {

        log.info("Saving Weekend Policy: {}", weekend);
        String orgId = jwtHelper.getOrganizationCode();
        String collectionName = attendanceWeekendPolicyRepository.getCollectionName(orgId);
        AttendanceWeekendPolicy weekendPolicy = new AttendanceWeekendPolicy();
        List<WeekEnd> weekList = new ArrayList<>();

        List<String> satDates = weekend.getSatDate();
        List<String> satStatuses = weekend.getSatStatus();
        List<String> sunDates = weekend.getSunDate();
        List<String> sunStatuses = weekend.getSunStatus();

        int size = satDates.size();
        weekendPolicy.setMonth(weekend.getMonth());
        for (int i = 0; i < size; i++) {
            WeekEnd weekEnd = new WeekEnd();
            weekEnd.setWeekName("Week " + (i + 1));

            if (i < satDates.size()) {
                weekEnd.setSatDate(satDates.get(i));
                weekEnd.setSatStatus(satStatuses.get(i));
            } else {
                weekEnd.setSatDate(null);
                weekEnd.setSatStatus(null);
            }

            if (i < sunDates.size()) {
                weekEnd.setSunDate(sunDates.get(i));
                weekEnd.setSunStatus(sunStatuses.get(i));
            } else {
                weekEnd.setSunDate(null);
                weekEnd.setSunStatus(null);
            }

            weekList.add(weekEnd);
        }

        weekendPolicy.setWeek(weekList);
        mongoTemplate.save(weekendPolicy, collectionName);
        return weekendPolicy;
    }

    @Override
    public List<AttendanceWeekendPolicy> getWeekendPolicy() {

        log.info("fetch WeekendPolicy details");
        String orgId = jwtHelper.getOrganizationCode();
        String collectionName = attendanceWeekendPolicyRepository.getCollectionName(orgId);
        return mongoTemplate.findAll(AttendanceWeekendPolicy.class, collectionName);
    }

    @Override
    public AttendanceWeekendPolicy getWeekendPolicyByMonth(String month) {

        log.info("fetch weekend Policy by month");
        String orgId = jwtHelper.getOrganizationCode();
        return attendanceWeekendPolicyRepository.findByMonth(month, orgId, mongoTemplate);
    }

    @Override
    public AttendanceWeekendPolicy updateWeekendPolicy(String month, AttendanceWeekendDTO weekend) {

        log.info("update weekend Policy by month");
        String orgId = jwtHelper.getOrganizationCode();
        String collectionName = attendanceWeekendPolicyRepository.getCollectionName(orgId);
        AttendanceWeekendPolicy existingPolicy = attendanceWeekendPolicyRepository.findByMonth(month, orgId,
                mongoTemplate);
        if (existingPolicy == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
        List<WeekEnd> weekList = new ArrayList<>();

        List<String> satDates = weekend.getSatDate();
        List<String> satStatuses = weekend.getSatStatus();
        List<String> sunDates = weekend.getSunDate();
        List<String> sunStatuses = weekend.getSunStatus();

        int maxSize = Math.max(satDates.size(), sunDates.size());
        for (int i = 0; i < maxSize; i++) {
            WeekEnd weekEnd = new WeekEnd();
            weekEnd.setWeekName("Week " + (i + 1));
            if (i < satDates.size()) {
                weekEnd.setSatDate(satDates.get(i));
                weekEnd.setSatStatus(satStatuses.get(i));
            } else {
                weekEnd.setSatDate(null);
                weekEnd.setSatStatus(null);
            }
            if (i < sunDates.size()) {
                weekEnd.setSunDate(sunDates.get(i));
                weekEnd.setSunStatus(sunStatuses.get(i));
            } else {
                weekEnd.setSunDate(null);
                weekEnd.setSunStatus(null);
            }
            weekList.add(weekEnd);
        }
        existingPolicy.setWeek(weekList);
        return mongoTemplate.save(existingPolicy, collectionName);
    }

    @Override
    public Map<String, Object> getWeekends(String monthYear) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("monthYear", monthYear);

        YearMonth yearMonth = YearMonth.parse(monthYear, DateTimeFormatter.ofPattern("MM-yyyy"));
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        int weekNumber = 1;
        LocalDate currentDate = firstDay;

        while (currentDate.isBefore(lastDay) || currentDate.isEqual(lastDay)) {
            LocalDate saturday = currentDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SATURDAY));
            LocalDate sunday = saturday.plusDays(1);
            if (saturday.isAfter(lastDay))
                break;
            Map<String, String> weekData = new LinkedHashMap<>();
            weekData.put("saturday", saturday.toString());
            weekData.put("sunday", sunday.isAfter(lastDay) ? null : sunday.toString());
            response.put("Week " + weekNumber, weekData);
            weekNumber++;
            currentDate = saturday.plusDays(7);
        }
        return response;
    }
}
