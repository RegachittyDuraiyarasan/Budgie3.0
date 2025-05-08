package com.hepl.budgie.service.impl.event;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.controller.events.WishesController;
import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.dto.event.WishesDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.event.Wishes;
import com.hepl.budgie.entity.event.WishesType;
import com.hepl.budgie.repository.events.WishesRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.event.WishesService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Service
@Slf4j
public class WishesServiceImpl implements WishesService {

    private final UserInfoRepository userInfoRepository;
    private final JWTHelper jwtHelper;
    private final MongoTemplate mongoTemplate;
    private final WishesRepository wishesRepository;

    @Override
    public List<EmployeeOrgChartDTO> getAnniversary(int limit, String yearAndMonth, String employee, WishesType type) {
        log.info("Fetching anniversary");
        int monthValue = 0;
        int dayValue = 0;
        int yearValue = 0;
        if (type == WishesType.MONTH) {
            YearMonth yearMonth = YearMonth.parse(yearAndMonth);
            monthValue = yearMonth.getMonthValue();
            yearValue = yearMonth.getYear();
        } else {
            LocalDateTime today = LocalDateTime.now();
            monthValue = today.getMonth().getValue();
            dayValue = today.getDayOfMonth();
            yearValue = today.getYear();
        }

        return userInfoRepository.getEmployeeListBasedOnMatchDayAndMonth(
                WishesDTO.builder().date(dayValue).month(monthValue).employee(employee).year(yearValue).build(),
                jwtHelper.getOrganizationCode(), "sections.workingInformation.doj", LocaleContextHolder.getTimeZone(),
                mongoTemplate, limit, type,jwtHelper.getUserRefDetail().getEmpId(),Status.ANNIVERSARY.getLabel()).getMappedResults();
    }

    @Override
    public List<EmployeeOrgChartDTO> getBirthdays(int limit, String yearAndMonth, String employee, WishesType type) {
        log.info("Fetching birthday");
        int monthValue = 0;
        int dayValue = 0;
        int yearValue = 0;
        if (type == WishesType.MONTH) {
            YearMonth yearMonth = YearMonth.parse(yearAndMonth);
            monthValue = yearMonth.getMonthValue();
            yearValue = yearMonth.getYear();
        } else {
            LocalDateTime today = LocalDateTime.now();
            monthValue = today.getMonth().getValue();
            dayValue = today.getDayOfMonth();
            yearValue = today.getYear();
        }
        return userInfoRepository.getEmployeeListBasedOnMatchDayAndMonth(
                WishesDTO.builder().date(dayValue).month(monthValue).employee(employee).year(yearValue).build(),
                jwtHelper.getOrganizationCode(), "sections.basicDetails.dob", LocaleContextHolder.getTimeZone(),
                mongoTemplate, limit, type,jwtHelper.getUserRefDetail().getEmpId(),Status.BIRTHDAY.getLabel()).getMappedResults();
    }

    @Override
    public void sendMail(String from, Status type) {

        String toEmp = jwtHelper.getUserRefDetail().getEmpId();
        Wishes wish = new Wishes();
        wish.setFrom(toEmp);
        wish.setTo(from);
        wish.setType(type.getLabel());
        wish.setTime(LocalDateTime.now());
        String cln = wishesRepository.getCollectionName(jwtHelper.getOrganizationCode());
        mongoTemplate.save(wish, cln);
    }

}
