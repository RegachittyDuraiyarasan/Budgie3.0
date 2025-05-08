package com.hepl.budgie.controller.events;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.event.EventDto;
import com.hepl.budgie.entity.event.Event;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.service.event.EventService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Create and Manage Events", description = "")
@RestController
@RequestMapping("/events")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class EventController {

    private final EventService eventService;
    private final JWTHelper jwtHelper;

    public EventController(EventService eventService, JWTHelper jwtHelper) {
        this.eventService = eventService;
        this.jwtHelper = jwtHelper;
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<Event> createEvent(@Valid @ModelAttribute EventDto event) throws IOException {
        String org = jwtHelper.getOrganizationCode();
        Event createdEvent = eventService.createEvent(event, org);
        return GenericResponse.success(createdEvent);
    }

    @GetMapping()
    public GenericResponse<List<Map<String, Object>>> getAllEvents(@RequestParam(required = false) String employee,
            @RequestParam(required = false) String category, @RequestParam(required = false) String eventType,
            @RequestParam(required = false, value = "month") String month) {
        String org = jwtHelper.getOrganizationCode();
        List<Map<String, Object>> events = eventService.getAllEvents(org, employee, category, eventType, month);
        return GenericResponse.success(events);
    }

    @GetMapping("/{id}")
    public GenericResponse<Event> getEventById(@PathVariable String id) {
        String org = jwtHelper.getOrganizationCode();
        Event event = eventService.getEventById(id, org);
        return GenericResponse.success(event);
    }

    @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<Event> updateEvent(@RequestParam String id, @Valid @ModelAttribute EventDto event)
            throws IOException {
        String org = jwtHelper.getOrganizationCode();
        Event updatedEvent = eventService.updateEvent(id, event, org);
        return GenericResponse.success(updatedEvent);
    }

    @DeleteMapping("{id}")
    public GenericResponse<Event> deleteEvent(@PathVariable String id) {
        String org = jwtHelper.getOrganizationCode();
        Event deletedEvent = eventService.deleteEvent(id, org);
        return GenericResponse.success(deletedEvent);
    }

    @GetMapping("/attendees")
    public GenericResponse<List<UserInfo>> getEventAttendees(@RequestParam(required = false) String key) {
        String org = jwtHelper.getOrganizationCode();
        List<UserInfo> attendees = eventService.getEventAttendees(org, key);
        return GenericResponse.success(attendees);
    }

    @GetMapping("/all-attendees")
    public GenericResponse<List<Map<String, Object>>> getAllEventAttendees(
            @RequestParam(required = false, value = "month") String month,
            @RequestParam(required = false, value = "year") String year) {
        List<Map<String, Object>> attendees = eventService.getEventAttendes(month,year);
        return GenericResponse.success(attendees);
    }
}
