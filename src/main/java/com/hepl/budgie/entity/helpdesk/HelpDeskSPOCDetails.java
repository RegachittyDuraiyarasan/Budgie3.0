package com.hepl.budgie.entity.helpdesk;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "m_helpdesk_spoc_details")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HelpDeskSPOCDetails {
    private String commonId;
    private String spocEmpId;
    private String menuId;
    private String subMenuId;
    private boolean status;
}
