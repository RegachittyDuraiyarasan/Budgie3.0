package com.hepl.budgie.controller.Holidays;

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
import com.hepl.budgie.dto.settings.HolidayDto;
import com.hepl.budgie.entity.settings.Holiday;
import com.hepl.budgie.service.HolidaysService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Admin Holidays", description = "")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/holidays")
public class HolidaysController {

    private final HolidaysService holidaysService;
    private final JWTHelper jwtHelper;

    @PostMapping(value = "/admin-holidays", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<Holiday> addAdminHolidays(@Valid @ModelAttribute HolidayDto holiday) throws IOException {
        String org = jwtHelper.getOrganizationCode();
        return GenericResponse.success(holidaysService.addAdminHolidays(holiday, org));
    }

    @GetMapping("/all-holidays")
    public GenericResponse<List<Map<String, Object>>> getAllHolidays(@RequestParam(required = false) String month,
            @RequestParam(required = false) String state, @RequestParam(required = false) String location,
            @RequestParam(required = false) String type) {
        String org = jwtHelper.getOrganizationCode();
        List<Map<String, Object>> holiday =  holidaysService.getAllHolidays(org, month, state, location, type);
        return GenericResponse.success(holiday);
    }

    @GetMapping("{id}")
    public GenericResponse<Holiday> getHolidayById(@PathVariable String id) {
        String org = jwtHelper.getOrganizationCode();
        Holiday holiday = holidaysService.getHolidayById(id, org);
        return GenericResponse.success(holiday);
    }

    @PutMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public GenericResponse<Holiday> updateHoliday(@RequestParam String id, @Valid @ModelAttribute HolidayDto holiday)
            throws IOException {
        String org = jwtHelper.getOrganizationCode();
        Holiday holidays = holidaysService.updateHoliday(id, holiday, org);
        return GenericResponse.success(holidays);
    }

    @DeleteMapping("{id}")
    public GenericResponse<Holiday> deleteHoliday(@PathVariable String id) {
        String org = jwtHelper.getOrganizationCode();
        Holiday holiday = holidaysService.deleteHoliday(id, org);
        return GenericResponse.success(holiday);
    }

}
