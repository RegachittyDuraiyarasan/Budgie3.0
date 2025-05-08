package com.hepl.budgie.repository.helpdesk;

import com.hepl.budgie.entity.helpdesk.HelpdeskTicketDetails;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HelpdeskTicketDetailsRepository extends MongoRepository<HelpdeskTicketDetails,String> {
    List<HelpdeskTicketDetails> findByTicketStatus(String ticketStatus);

    boolean existsByTicketId(String ticketId);

    HelpdeskTicketDetails findByTicketId(String tickId);
}
