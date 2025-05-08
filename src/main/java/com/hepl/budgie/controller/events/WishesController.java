package com.hepl.budgie.controller.events;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.employee.EmployeeOrgChartDTO;
import com.hepl.budgie.entity.event.WishesType;
import com.hepl.budgie.service.event.WishesService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

import java.io.ObjectInputFilter.Status;
import java.util.List;

@Tag(name = "Create and Manage wishes", description = "")
@RestController
@RequiredArgsConstructor
@RequestMapping("/events/wishes")
@SecurityRequirement(name = "Bearer Authentication")
@Slf4j
public class WishesController {

    private final WishesService wishesService;

    @GetMapping("/birthdays")
    public GenericResponse<List<EmployeeOrgChartDTO>> getBirthdays(
            @RequestParam(required = false, defaultValue = "") String yearAndMonth,
            @RequestParam(required = false, defaultValue = "") String employee,
            @RequestParam WishesType type) {
        log.info("Get birthdays");

        return GenericResponse.success(wishesService.getBirthdays(0, yearAndMonth, employee, type));
    }

    @GetMapping("/anniversary")
    public GenericResponse<List<EmployeeOrgChartDTO>> getAnniversary(
            @RequestParam(required = false, defaultValue = "") String yearAndMonth,
            @RequestParam(required = false, defaultValue = "") String employee,
            @RequestParam WishesType type) {
        log.info("Get anniversaries");

        return GenericResponse.success(wishesService.getAnniversary(0, yearAndMonth, employee, type));
    }

    @PutMapping()
    public GenericResponse<String> sendMail(@RequestParam String from, @RequestParam com.hepl.budgie.entity.Status type) {
        log.info("Send mail to employee {}", from);
        wishesService.sendMail(from, type);
        return GenericResponse.success("Mail sent successfully");
    }

}
