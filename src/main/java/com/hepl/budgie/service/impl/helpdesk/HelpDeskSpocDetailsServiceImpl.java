package com.hepl.budgie.service.impl.helpdesk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.helpdesk.EmployeeDto;
import com.hepl.budgie.entity.helpdesk.HelpDeskSPOCDetails;
import com.hepl.budgie.repository.helpdesk.HelpDeskSpocDetailsRepository;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.helpdesk.HelpDeskSpocDetailsService;
import com.hepl.budgie.utils.AppMessages;
import io.swagger.v3.oas.models.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HelpDeskSpocDetailsServiceImpl implements HelpDeskSpocDetailsService {

    private final HelpDeskSpocDetailsRepository helpDeskRepository;

    private final MasterSettingsRepository masterSettingsRepository;

    private final UserInfoRepository userInfoRepository;

    private final MongoTemplate mongoTemplate;

    private final JWTHelper jwtHelper;

    private final WebClient webClient;

    @Override
    public void addSPOCDetails(HelpDeskSPOCDetails helpDeskSPOCDetails, String org) {
        log.info("helpDeskSPOCDetails {} ", helpDeskSPOCDetails);
        helpDeskRepository.addSpoc(mongoTemplate, helpDeskSPOCDetails, jwtHelper.getOrganizationCode());
    }

    private Mono<String> webClients(String path) {
        return webClient.get()
                .uri(builder -> builder.path(path)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .onErrorResume(e -> Mono.just("Error occurred: " + e.getMessage()));
    }


    @Override
    public Mono<GenericResponse<List<EmployeeDto>>> getEmployeeDetails() {
        String path = "/api/get_active_employee";
        Mono<String> response = webClients(path);

        return response.flatMap(data -> {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                List<EmployeeDto> employeeList = objectMapper.readValue(
                        data,
                        new TypeReference<>() {
                        }
                );
                return Mono.just(GenericResponse.success(employeeList));
            } catch (Exception e) {
                log.error("Failed to parse employee data", e);
                return Mono.just(GenericResponse.success(Collections.emptyList()));
            }
        });
    }


}
