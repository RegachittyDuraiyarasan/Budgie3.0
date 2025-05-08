package com.hepl.budgie.service.impl.helpdesk;

import com.hepl.budgie.controller.helpdesk.MyRequestController;
import com.hepl.budgie.dto.helpdesk.*;
import com.hepl.budgie.entity.helpdesk.FileDetails;
import com.hepl.budgie.entity.helpdesk.HelpdeskTicketDetails;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;
import com.hepl.budgie.repository.helpdesk.HelpdeskTicketDetailsRepository;
import com.hepl.budgie.repository.master.MasterSettingsRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.helpdesk.MyRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MyRequestServiceImpl implements MyRequestService {
    private final HelpdeskTicketDetailsRepository helpdeskTicketDetailsRepository;
    private final UserInfoRepository userInfoRepository;
    private final MasterSettingsRepository masterSettingsRepository;
    private final MongoTemplate mongoTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override

    public List<Map<String, Object>> fetchMyRequest(String referenceName, String org) {
        List<HelpdeskTicketDetails> tickets = helpdeskTicketDetailsRepository.findByTicketStatus("Pending");

        return tickets.stream().map(ticket -> {
            Map<String, Object> ticketMap = new HashMap<>();

            ticketMap.put("ticketId", ticket.getTicketId());
            ticketMap.put("employeeName", getEmployeeName(ticket.getEmpId()));
            ticketMap.put("raisedFor", getEmployeeName(ticket.getForEmpId()));
            ticketMap.put("spocName", getEmployeeName(ticket.getSpocEmpId()));
            ticketMap.put("category", getCategoryName(ticket.getCategoryId(), referenceName, org));
            ticketMap.put("concerns", ticket.getTicketDetails());

            String closureDate = ticket.getTicketCreatedOn() != null
                    ? ticket.getTicketCreatedOn().format(DATE_FORMATTER)
                    : null;
            ticketMap.put("closure", closureDate);
            ticketMap.put("aging", ticket.getAgeing() + " Days");

            if (ticket.getFileDetails() != null) {
                FileDetails fileDetails = ticket.getFileDetails();
                Map<String, String> supportingFileMap = new HashMap<>();
                supportingFileMap.put("fileName", fileDetails.getFileName());
                supportingFileMap.put("folderName", fileDetails.getFoldername());

                ticketMap.put("supporting", supportingFileMap);
            }

            ticketMap.put("status", ticket.getTicketStatus());
            return ticketMap;
        }).collect(Collectors.toList());
    }

    private String getEmployeeName(String empId) {
        if (empId == null) return null;
        return userInfoRepository.findByEmpId(empId)
                .map(user -> user.getSections().getBasicDetails().getFirstName() + " - " + user.getEmpId())
                .orElse(null);
    }
    private String getEmpName(String empId) {
        if (empId == null) return null;
        return userInfoRepository.findByEmpId(empId)
                .map(user -> user.getSections().getBasicDetails().getFirstName())
                .orElse(null);
    }

    private String getCategoryName(String categoryId, String referenceName, String org) {
        if (categoryId == null) return null;

        Optional<MasterFormSettings> masterForm = masterSettingsRepository.fetchOptions(referenceName, org, mongoTemplate);

        return masterForm.flatMap(settings -> settings.getOptions().stream()
                        .filter(option -> categoryId.equals(option.getCategoryId()))
                        .map(MasterFormOptions::getName)
                        .findFirst())
                .orElse(null);
    }

    private String calculateAging(LocalDate ticketCreatedOn) {
        if (ticketCreatedOn == null) return "0";

        LocalDate today = LocalDate.now(ZoneId.of("UTC"));
        long daysBetween = ChronoUnit.DAYS.between(ticketCreatedOn, today);

        return String.valueOf(daysBetween);
    }


    @Override
    public List<Map<String, Object>> fetchMyRequestCompleted(String referenceName, String org) {
        List<HelpdeskTicketDetails> tickets = helpdeskTicketDetailsRepository.findByTicketStatus("Completed");

        return tickets.stream().map(ticket -> {
            Map<String, Object> ticketMap = new HashMap<>();

            ticketMap.put("ticketId", ticket.getTicketId());
            ticketMap.put("employeeName", getEmployeeName(ticket.getEmpId()));
            ticketMap.put("raisedFor", getEmployeeName(ticket.getForEmpId()));
            ticketMap.put("spocName", getEmployeeName(ticket.getSpocEmpId()));
            ticketMap.put("category", getCategoryName(ticket.getCategoryId(), referenceName, org));
            ticketMap.put("concerns", ticket.getTicketDetails());

            String closureDate = ticket.getTicketCreatedOn() != null
                    ? ticket.getTicketCreatedOn().format(DATE_FORMATTER)
                    : null;
            ticketMap.put("closure", closureDate);
            ticketMap.put("aging", ticket.getAgeing() + " Days");

            if (ticket.getFileDetails() != null) {
                FileDetails fileDetails = ticket.getFileDetails();
                Map<String, String> supportingFileMap = new HashMap<>();
                supportingFileMap.put("fileName", fileDetails.getFileName());
                supportingFileMap.put("folderName", fileDetails.getFoldername());

                ticketMap.put("supporting", supportingFileMap);
            }

            ticketMap.put("status", ticket.getTicketStatus());
            return ticketMap;
        }).collect(Collectors.toList());
    }

    @Override
    public String updateRemarks(HelpdeskRemarksDTO helpdeskRemarksDTO, String tickId) {
        HelpdeskTicketDetails helpdeskTicketDetails = helpdeskTicketDetailsRepository.findByTicketId(tickId);

        if (helpdeskTicketDetails == null) {
            log.warn("Ticket ID {} not found!", tickId);
            return "Ticket Not Found";
        }
        helpdeskTicketDetails.setTicketStatus(helpdeskRemarksDTO.getStatus());

        if ("Completed".equalsIgnoreCase(helpdeskRemarksDTO.getStatus())) {
            helpdeskTicketDetails.setSpocRemarks(helpdeskRemarksDTO.getRemarks());
            helpdeskTicketDetails.setTicketClosedDate(ZonedDateTime.now());
        }

        helpdeskTicketDetailsRepository.save(helpdeskTicketDetails);
        log.info("Ticket ID {} updated successfully with status: {}", tickId, helpdeskRemarksDTO.getStatus());

        return "Ticket Updated Successfully";
    }

    @Override
    public List<HrHelpDeskReportDTO> fetchHrHelpDeskReport(
            String referenceName, String org, HelpDeskReportFilterDTO filter) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ZonedDateTime startDate = Optional.ofNullable(filter.getStartDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atStartOfDay(ZoneId.systemDefault()))
                .orElse(null);

        ZonedDateTime endDate = Optional.ofNullable(filter.getEndDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atTime(23, 59, 59).atZone(ZoneId.systemDefault()))
                .orElse(null);

        List<HelpdeskTicketDetails> tickets = helpdeskTicketDetailsRepository.findAll();

        return tickets.stream()
                .filter(ticket -> {
                    String categoryName = getCategoryNameFromSettings(ticket.getCategoryId(), referenceName, org);
                    return filter.getCategory() == null || filter.getCategory().isEmpty() || categoryName.equalsIgnoreCase(filter.getCategory());
                })
                .filter(ticket -> (filter.getStatus() == null || filter.getStatus().isEmpty() || ticket.getTicketStatus().equalsIgnoreCase(filter.getStatus())))
                .filter(ticket -> (startDate == null || ticket.getTicketCreatedOn().isAfter(startDate) || ticket.getTicketCreatedOn().isEqual(startDate)))
                .filter(ticket -> (endDate == null || ticket.getTicketCreatedOn().isBefore(endDate) || ticket.getTicketCreatedOn().isEqual(endDate)))
                .map(ticket -> {
                    HrHelpDeskReportDTO hrHelpDeskReportDTO = new HrHelpDeskReportDTO();
                    hrHelpDeskReportDTO.setTicketID(ticket.getTicketId());
                    hrHelpDeskReportDTO.setEmployeeName(getEmpName(ticket.getEmpId()));
                    hrHelpDeskReportDTO.setRaisedFor(getEmpName(ticket.getForEmpId()));
                    hrHelpDeskReportDTO.setSpocName(getEmpName(ticket.getSpocEmpId()));
                    hrHelpDeskReportDTO.setStatus(ticket.getTicketStatus());

                    String categoryName = getCategoryNameFromSettings(ticket.getCategoryId(), referenceName, org);
                    hrHelpDeskReportDTO.setCategory(categoryName);

                    hrHelpDeskReportDTO.setConcerns(ticket.getTicketDetails());
                    hrHelpDeskReportDTO.setSpocRemarks(ticket.getSpocRemarks());
                    hrHelpDeskReportDTO.setAging(ticket.getAgeing() + " days");

                    String status = Optional.ofNullable(ticket.getTicketStatus())
                            .map(String::trim)
                            .map(String::toLowerCase)
                            .orElse("");
                    String ticketLifecycle = (status.contains("pending") || status.contains("inprocess") || status.contains("in progress"))
                            ? "Open"
                            : (status.contains("completed") ? "Closed" : "Unknown");

                    hrHelpDeskReportDTO.setTicketLifecycle(ticketLifecycle);

                    hrHelpDeskReportDTO.setRaisedDate(
                            Optional.ofNullable(ticket.getTicketCreatedOn())
                                    .map(date -> date.format(formatter))
                                    .orElse(null)
                    );
                    hrHelpDeskReportDTO.setClosureDate(
                            Optional.ofNullable(ticket.getTicketClosedDate())
                                    .map(date -> date.format(formatter))
                                    .orElse(null)
                    );

                    return hrHelpDeskReportDTO;
                }).collect(Collectors.toList());

    }
        private String getCategoryNameFromSettings(String categoryId, String referenceName, String org) {
        Optional<MasterFormSettings> settingsOpt = masterSettingsRepository
                .fetchOptions(referenceName, org,mongoTemplate);

        if (settingsOpt.isPresent()) {
            List<MasterFormOptions> options = settingsOpt.get().getOptions();
            System.out.println("options--"+options);
            return options.stream()
                    .filter(option -> option.getCategoryId().equals(categoryId))
                    .map(MasterFormOptions::getName)
                    .findFirst()
                    .orElse("Unknown");
        }


        return "Unknown";
    }
    @Override
    public HelpDeskMovementGraphDTO fetchCount(String referenceName, String org, HelpDeskMovementFilterDTO filter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ZonedDateTime startDate = Optional.ofNullable(filter.getStartDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atStartOfDay(ZoneId.systemDefault()))
                .orElse(null);

        ZonedDateTime endDate = Optional.ofNullable(filter.getEndDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atTime(23, 59, 59).atZone(ZoneId.systemDefault()))
                .orElse(null);

        List<HelpdeskTicketDetails> tickets = helpdeskTicketDetailsRepository.findAll();
        long withAt = tickets.stream()
                .filter(ticket -> {
                    String categoryName = getCategoryNameFromSettings(ticket.getCategoryId(), referenceName, org);
                    return filter.getCategory() == null || filter.getCategory().isEmpty() || categoryName.equalsIgnoreCase(filter.getCategory());
                })
                .filter(ticket -> startDate == null || ticket.getTicketCreatedOn().isAfter(startDate) || ticket.getTicketCreatedOn().isEqual(startDate))
                .filter(ticket -> endDate == null || ticket.getTicketCreatedOn().isBefore(endDate) || ticket.getTicketCreatedOn().isEqual(endDate))
                .filter(ticket -> ticket.getAgeing() <= 2)
                .count();

        long withoutAt = tickets.stream()
                .filter(ticket -> {
                    String categoryName = getCategoryNameFromSettings(ticket.getCategoryId(), referenceName, org);
                    return filter.getCategory() == null || filter.getCategory().isEmpty() || categoryName.equalsIgnoreCase(filter.getCategory());
                })
                .filter(ticket -> startDate == null || ticket.getTicketCreatedOn().isAfter(startDate) || ticket.getTicketCreatedOn().isEqual(startDate))
                .filter(ticket -> endDate == null || ticket.getTicketCreatedOn().isBefore(endDate) || ticket.getTicketCreatedOn().isEqual(endDate))
                .filter(ticket -> ticket.getAgeing() >= 3)
                .count();

        HelpDeskMovementGraphDTO response = new HelpDeskMovementGraphDTO();
        response.setWithAt((int) withAt);
        response.setWithoutAt((int) withoutAt);

        return response;
    }


    @Override
    public HelpDeskGraphDTO fetchTicketStatus(String referenceName, String org, HelpDeskReportFilterDTO filter) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        ZonedDateTime startDate = Optional.ofNullable(filter.getStartDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atStartOfDay(ZoneId.systemDefault()))
                .orElse(null);

        ZonedDateTime endDate = Optional.ofNullable(filter.getEndDate())
                .filter(date -> !date.isEmpty())
                .map(date -> LocalDate.parse(date, formatter).atTime(23, 59, 59).atZone(ZoneId.systemDefault()))
                .orElse(null);

        List<HelpdeskTicketDetails> tickets = helpdeskTicketDetailsRepository.findAll();

        List<HelpdeskTicketDetails> filteredTickets = tickets.stream()
                .filter(ticket -> {
                    String categoryName = getCategoryNameFromSettings(ticket.getCategoryId(), referenceName, org);
                    return filter.getCategory() == null || filter.getCategory().isEmpty() || categoryName.equalsIgnoreCase(filter.getCategory());
                })
                .filter(ticket -> filter.getStatus() == null || filter.getStatus().isEmpty() || ticket.getTicketStatus().equalsIgnoreCase(filter.getStatus()))
                .filter(ticket -> startDate == null || ticket.getTicketCreatedOn().isAfter(startDate) || ticket.getTicketCreatedOn().isEqual(startDate))
                .filter(ticket -> endDate == null || ticket.getTicketCreatedOn().isBefore(endDate) || ticket.getTicketCreatedOn().isEqual(endDate))
                .collect(Collectors.toList());  // Collect stream to List to reuse it

        long completed = filteredTickets.stream().filter(ticket -> "Completed".equalsIgnoreCase(ticket.getTicketStatus())).count();
        long pending = filteredTickets.stream().filter(ticket -> "Pending".equalsIgnoreCase(ticket.getTicketStatus())).count();
        long inprogress = filteredTickets.stream().filter(ticket -> "Inprocess".equalsIgnoreCase(ticket.getTicketStatus())).count();

        HelpDeskGraphDTO response = new HelpDeskGraphDTO();
        response.setCompleted(String.valueOf(completed));
        response.setPending(String.valueOf(pending));
        response.setInprogress(String.valueOf(inprogress));

        return response;
    }



}