package com.hepl.budgie.service.impl.helpdesk;

import com.hepl.budgie.dto.helpdesk.HelpdeskTickDTO;
import com.hepl.budgie.entity.FileType;
import com.hepl.budgie.entity.helpdesk.HelpDeskSPOCDetails;
import com.hepl.budgie.entity.helpdesk.FileDetails;
import com.hepl.budgie.entity.helpdesk.HelpdeskTicketDetails;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.repository.helpdesk.HelpdeskTicketDetailsRepository;
import com.hepl.budgie.repository.helpdesk.HelpDeskSpocDetailsRepository;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.FileService;
import com.hepl.budgie.service.helpdesk.HelpdeskTicketDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HelpdeskTicketDetailsServiceImpl implements HelpdeskTicketDetailsService {

    private final HelpdeskTicketDetailsRepository helpdeskTicketRepository;
    private final HelpDeskSpocDetailsRepository helpDeskSpocDetailsRepository;
    private final UserInfoRepository userInfoRepository;
    private final FileService fileService;
    private final MasterSettingsRepository masterSettingsRepository;
    private final MongoTemplate mongoTemplate;

    private static final AtomicInteger counter = new AtomicInteger(1);

    @Override
    public HelpdeskTicketDetails addHelpdeskTicket(HelpdeskTickDTO helpdeskTickDTO, String empId) {
        log.info("Adding new helpdesk ticket for employee: {}", empId);

        String ticketId;
        do {
            ticketId = String.format("HD%03d", counter.getAndIncrement());
        } while (helpdeskTicketRepository.existsByTicketId(ticketId));

        HelpdeskTicketDetails ticketDetails = new HelpdeskTicketDetails();
        ticketDetails.setTicketId(ticketId);
        ticketDetails.setEmpId(empId);
        ticketDetails.setTicketCreatedOn(ZonedDateTime.now());
        ticketDetails.setCategoryId(helpdeskTickDTO.getCategory());
        ticketDetails.setTicketDetails(helpdeskTickDTO.getDetails());

        if ("MySelf".equalsIgnoreCase(helpdeskTickDTO.getTicketRaisedAt())) {
            ticketDetails.setSelfRequest("Yes");
        } else {
            ticketDetails.setSelfRequest("No");
            ticketDetails.setForEmpId(helpdeskTickDTO.getEmployeeCode());
        }

//        Optional<HelpDeskSPOCDetails> spocDetailsOpt = helpDeskSpocDetailsRepository.findByCategoryId(helpdeskTickDTO.getCategory());
//        spocDetailsOpt.ifPresent(spocDetails -> {
//            ticketDetails.setSpocEmpId(spocDetails.getSpocEmpId());
//            userInfoRepository.findByEmpId(spocDetails.getSpocEmpId()).ifPresent(user ->
//                    log.info("UserInfo found for SPOC Emp ID: {}", spocDetails.getSpocEmpId()));
//        });

        MultipartFile file = helpdeskTickDTO.getFile();
        if (file != null && !file.isEmpty()) {
            String folderName = "HELPDESK";
            String generatedFileName = generateFileName(empId, folderName);

            try {
                String storedFilePath = fileService.uploadFile(file, FileType.valueOf(folderName), generatedFileName);
                FileDetails fileDetails = new FileDetails(folderName, storedFilePath);
                ticketDetails.setFileDetails(fileDetails);
            } catch (IllegalArgumentException | IOException e) {
                log.error("File upload failed!", e);
            }
        }

        ticketDetails.setTicketStatus("Pending");
        ticketDetails.setSpocRemarks(null);

        HelpdeskTicketDetails savedTicket = helpdeskTicketRepository.save(ticketDetails);
        log.info("Helpdesk ticket saved successfully with Ticket ID: {}", savedTicket.getTicketId());

        return savedTicket;
    }

    @Override
    public List<Map<String, String>> getAllCategoryIds(String referenceName, String org) {
        Optional<MasterFormSettings> masterForm = masterSettingsRepository.fetchOptions(referenceName, org, mongoTemplate);

        if (masterForm.isPresent()) {
            return masterForm.get().getOptions().stream()
                    .map(option -> {
                        Map<String, String> categoryMap = new HashMap<>();
                        categoryMap.put("categoryId", option.getCategoryId());
                        categoryMap.put("name", option.getName());
                        return categoryMap;
                    })
                    .collect(Collectors.toList());
        }

        return List.of();
    }





    private String generateFileName(String empId, String folderName) {
        return empId + "_"+ folderName;
    }
}
