package com.hepl.budgie.entity.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.ZonedDateTime;
@Data
@Document(collection = "t_helpdesk_ticket_details")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelpdeskTicketDetails {

    @Id
    private String id;
    private String ticketId;
    private ZonedDateTime ticketCreatedOn;
    private String empId;
    private String selfRequest;
    private String forEmpId;
    private String categoryId;
    private String ticketDetails;
    private FileDetails fileDetails;
    private String ticketStatus;
    private ZonedDateTime ticketClosedDate;
    private String spocEmpId;
    private String spocRemarks;
    private int ageing;

}
