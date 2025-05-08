package com.hepl.budgie.service.impl;

import java.io.IOException;
import java.io.ObjectInputFilter.Status;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.dto.countries.StateDTO;
import com.hepl.budgie.dto.settings.HolidayDto;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.mapper.setting.HolidayMapper;
import com.hepl.budgie.repository.master.HolidayRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.HolidaysService;
import com.hepl.budgie.service.general.CountriesAPIService;
import com.hepl.budgie.service.master.MasterSettingsService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;
@Service
@Slf4j
public class HolidaysServiceImpl implements HolidaysService {
    
    private final HolidayRepository holidayRepository;
    private final HolidayMapper holidayMapper;
    private final FileService fileService;
    private final MongoTemplate mongoTemplate;
    private CountriesAPIService countriesApiService;
    private MasterSettingsService masterSettingsService;

    public HolidaysServiceImpl(HolidayRepository holidayRepository, HolidayMapper holidayMapper, FileService fileService, MongoTemplate mongoTemplate,CountriesAPIService countriesApiService, MasterSettingsService masterSettingsService) {
        this.holidayRepository = holidayRepository;
        this.holidayMapper = holidayMapper;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.countriesApiService = countriesApiService;
        this.masterSettingsService = masterSettingsService;
    }

    @Override
    public Holiday addAdminHolidays(HolidayDto holidayDto, String org) throws IOException {
        log.info("Add admin holidays - {}", holidayDto.getOccasion());
        
        String collectionName = holidayRepository.getCollectionName(org);
        LocalDate date = LocalDate.parse(holidayDto.getDate());
        int holidayYear = date.getYear();

        Query query = new Query(Criteria.where("occasion").is(holidayDto.getOccasion())
        .and("date").gte(LocalDate.of(holidayYear, 1, 1))
        .lt(LocalDate.of(holidayYear + 1, 1, 1)));

        boolean holidayExists = mongoTemplate.exists(query, Holiday.class, collectionName);
        if (holidayExists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppMessages.HOLIDAY);
        }
        MultipartFile fil = holidayDto.getFile();
        String path = null;
        if(fil != null) {
            path = fileService.uploadFile(fil, FileType.HOLIDAY, "");
        }
        Holiday holiday = holidayMapper.toHoliday(holidayDto);
        holiday.setFile(path);
        holiday.setRestrictedHoliday(holidayDto.getRestrictedHoliday().getLabel());
        holiday.setStatus("Active");
        String dayOfWeek = date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH); // e.g., "Monday"
        holiday.setDay(dayOfWeek);
        mongoTemplate.save(holiday, collectionName);
        return holiday;
    }

   @Override
    public List<Map<String, Object>> getAllHolidays(String org, String month, String state, String location, String type) {
        
        String collectionName = holidayRepository.getCollectionName(org);
        List<Criteria> andCriteriaList = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where("status").is("Active"));

        if (month != null) {
            LocalDate startDate = LocalDate.parse(month + "-01");
            LocalDate endDate = startDate.plusMonths(1);
            query.addCriteria(Criteria.where("date").gte(startDate).lt(endDate));
        }

        if (state != null) {
            andCriteriaList.add(Criteria.where("stateList").in(state));
        }
        if (location != null) {
            andCriteriaList.add(Criteria.where("locationList").in(location));
        }
        if (type != null) {
            query.addCriteria(Criteria.where("restrictedHoliday").is(type));
        }

        if (!andCriteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(andCriteriaList.toArray(new Criteria[0])));
        }

        List<Holiday> holidays = mongoTemplate.find(query, Holiday.class, collectionName);
        
        List<StateDTO> allStates = countriesApiService.allStates();
        List<MasterFormOptions> allLocations = masterSettingsService.getSettingsByReferenceName("Work Location");

        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Holiday holiday : holidays) {
            String rest = holiday.getRestrictedHoliday();
            Map<String, Object> holidayMap = new HashMap<>();
            holidayMap.put("id", holiday.getId());
            holidayMap.put("occasion", holiday.getOccasion());
            holidayMap.put("restrictedHoliday", rest);
            holidayMap.put("status", holiday.getStatus());
            holidayMap.put("date", holiday.getDate());
            holidayMap.put("createdByUser", holiday.getCreatedByUser());
            holidayMap.put("modifiedByUser", holiday.getModifiedByUser());
            holidayMap.put("allState", holiday.isAllState());
            holidayMap.put("allLocation", holiday.isAllLocation());
            holidayMap.put("discription", holiday.getDiscription());
            holidayMap.put("file", holiday.getFile());

            if (holiday.isAllState()) {
                holidayMap.put("stateList", allStates);
            } else {
                holidayMap.put("stateList", holiday.getStateList());
            }

            if (holiday.isAllLocation()) {
                holidayMap.put("locationList", allLocations);
            } else {
                holidayMap.put("locationList", holiday.getLocationList());
            }

            result.add(holidayMap);
        }

        return result;
    }


    @Override
    public Holiday getHolidayById(String id, String org) {
        String collectionName = holidayRepository.getCollectionName(org);
        return mongoTemplate.findById(id, Holiday.class, collectionName);
    }

    @Override
    public Holiday updateHoliday(String id, HolidayDto holiday, String org) throws IOException {
        log.info("Update holiday - {}", holiday.getOccasion());
        String collectionName = holidayRepository.getCollectionName(org);
        Holiday existingHoliday = mongoTemplate.findById(id, Holiday.class, collectionName);
        if (existingHoliday == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.HOLIDAY_NOT_FOUND);
        }
        MultipartFile fil = holiday.getFile();
        String path = null;
        if(fil != null) {
            path = fileService.uploadFile(fil, FileType.HOLIDAY, "");
        }
        Holiday holiday1 = holidayMapper.toUpHolidays(holiday,existingHoliday);
        holiday1.setFile(path);
        holiday1.setRestrictedHoliday(holiday.getRestrictedHoliday().getLabel());
        mongoTemplate.save(holiday1, collectionName);
        return holiday1;
    }

    @Override
    public Holiday deleteHoliday(String id, String org) {
        String collectionName = holidayRepository.getCollectionName(org);
        Holiday holiday = mongoTemplate.findById(id, Holiday.class, collectionName);
        if (holiday == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.HOLIDAY_NOT_FOUND);
        }
        holiday.setStatus("Inactive");
        mongoTemplate.save(holiday, collectionName);
        return holiday;
    }

	@Override
	public Optional<Holiday> findHolidayByDate(LocalDate localDate, String org) {
		LocalDateTime startOfDay = localDate.atStartOfDay(); 
		LocalDateTime startOfNextDay = localDate.plusDays(1).atStartOfDay();
        return holidayRepository.findByDateRange(startOfDay, startOfNextDay, org, mongoTemplate);
    }
}
