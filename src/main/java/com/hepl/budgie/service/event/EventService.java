package com.hepl.budgie.service.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.hepl.budgie.dto.event.EventDto;
import com.hepl.budgie.entity.event.Event;
import com.hepl.budgie.entity.userinfo.UserInfo;

public interface EventService {

    Event createEvent(EventDto event,String org) throws IOException;

    List<Map<String, Object>> getAllEvents(String org,String employee,String category,String eventType,String month);

    Event getEventById(String id, String org);

    Event updateEvent(String id, EventDto event, String org) throws IOException;

    Event deleteEvent(String id, String org);

    List<UserInfo> getEventAttendees(String org,String key);

    List<Map<String, Object>> getEventAttendes(String month,String year);   
    
}
