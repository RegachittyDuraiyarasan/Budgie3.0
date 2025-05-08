package com.hepl.budgie.controller.dashboard;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.dashboard.DasboardResponseDTO;
import com.hepl.budgie.entity.event.WishesType;
import com.hepl.budgie.service.event.WishesService;

import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/dashboard")
@Slf4j
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
public class DashboardController {

    private WishesService wishesService;

    @GetMapping
    public GenericResponse<DasboardResponseDTO> getDashboardDetails() {
        log.info("Get dashboard details");

        return GenericResponse.success(
                DasboardResponseDTO.builder().birthdays(wishesService.getBirthdays(10, "", "", WishesType.TODAY))
                        .workAnniversary(wishesService.getAnniversary(10, "", "", WishesType.TODAY)).build());
    }

}
