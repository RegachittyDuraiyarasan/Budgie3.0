package com.hepl.budgie.service.impl.event;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.event.EventDto;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.event.Event;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.event.EventMapper;
import com.hepl.budgie.repository.events.EventRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.event.EventService;
import com.hepl.budgie.utils.AppMessages;

@Service
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final FileService fileService;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;
    private final UserInfoRepository userInfoRepository;

    public EventServiceImpl(EventRepository eventRepository, EventMapper eventMapper, FileService fileService,MongoTemplate mongoTemplate,JWTHelper jwtHelper,UserInfoRepository userInfoRepository) {
        this.eventRepository = eventRepository;
        this.eventMapper = eventMapper;
        this.fileService = fileService;
        this.mongoTemplate = mongoTemplate;
        this.jwtHelper = jwtHelper;
        this.userInfoRepository = userInfoRepository;
    }
    @Override
    public Event createEvent(EventDto event, String org) throws IOException {

        MultipartFile fil = event.getEventFile();
        String path = null;
        if(fil != null) {
            path = fileService.uploadFile(fil, FileType.EVENTS, "");
        }
        Event newEvent = eventMapper.eventDtoToEvent(event);
        newEvent.setEventFile(path);
        String collectionName = eventRepository.getCollectionName(org);

        newEvent.setStatus("Active");
        mongoTemplate.save(newEvent, collectionName);
        return newEvent;
    }
    @Override
    public List<Map<String, Object>> getAllEvents(String org, String employee, String category, String eventType, String month) {
        String collectionName = eventRepository.getCollectionName(org);
        return eventRepository.findAllEvents(collectionName, employee, category, eventType, mongoTemplate,userInfoRepository,month);
    }

    @Override
    public Event getEventById(String id, String org) {
        String collectionName = eventRepository.getCollectionName(org);
        return mongoTemplate.findById(id, Event.class, collectionName);
    }
    @Override
    public Event updateEvent(String id, EventDto event, String org) throws IOException {
        String collectionName = eventRepository.getCollectionName(org);
        Event existingEvent = mongoTemplate.findById(id, Event.class, collectionName);
        if (existingEvent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.EVENT);
        }
        MultipartFile fil = event.getEventFile();
        String path = null;
        if(fil != null) {
            path = fileService.uploadFile(fil, FileType.EVENTS, "");
        }
        Event events = eventMapper.toUpEvents(event,existingEvent);
        events.setEventFile(path);
        mongoTemplate.save(events, collectionName);
        return events;

    }
    @Override
    public Event deleteEvent(String id, String org) {
        String collectionName = eventRepository.getCollectionName(org);
        Event deletedEvent = mongoTemplate.findById(id, Event.class, collectionName);
        if (deletedEvent == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.EVENT);
        }
        deletedEvent.setStatus("Inactive");
        mongoTemplate.save(deletedEvent, collectionName);
        return deletedEvent;
    }

    @Override
    public List<UserInfo> getEventAttendees(String org, String key) {
        return eventRepository.getAttendees(org, key, mongoTemplate);
    }
    @Override
    public List<Map<String, Object>> getEventAttendes(String month,String year) {

        String empId = jwtHelper.getUserRefDetail().getEmpId();
        Optional<UserInfo> optionalUser = userInfoRepository.findByEmpId(empId);
        if (optionalUser.isEmpty()) {
            return Collections.emptyList(); 
        }
        UserInfo user = optionalUser.get();

        String designation = user.getSections().getWorkingInformation().getDesignation();
        String department = user.getSections().getWorkingInformation().getDepartment();
        String workLocation = user.getSections().getWorkingInformation().getWorkLocation();
        String gender = user.getSections().getBasicDetails().getGender();

        return eventRepository.findMatchingEvents(designation, department, workLocation, gender, mongoTemplate, jwtHelper.getOrganizationCode(),empId,month,userInfoRepository,year);
    }

}
