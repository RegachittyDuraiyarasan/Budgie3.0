package com.hepl.budgie.entity.preonboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.xmlbeans.impl.xb.xsdschema.Public;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BuddyFeedbackResponse {
    private Integer fieldNo;
    private String fieldName;
    private String selectedOption;
    private String remark;
    private String remarks;
    private String agrees;

}
