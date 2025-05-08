package com.hepl.budgie.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.hepl.budgie.dto.settings.HolidayDto;
import com.hepl.budgie.entity.settings.Holiday;

public interface HolidaysService {

    Holiday addAdminHolidays(HolidayDto holiday, String org) throws IOException;

    List<Map<String, Object>> getAllHolidays(String org, String month, String state, String location, String type);

    Holiday getHolidayById(String id, String org);

    Holiday updateHoliday(String id, HolidayDto holiday, String org) throws IOException;

    Holiday deleteHoliday(String id, String org);

    Optional<Holiday> findHolidayByDate(LocalDate date, String org);
}
