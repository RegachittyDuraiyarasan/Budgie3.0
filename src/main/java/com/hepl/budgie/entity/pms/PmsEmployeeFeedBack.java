package com.hepl.budgie.entity.pms;

import lombok.Data;
import org.springframework.data.annotation.Id;

@Data
public class PmsEmployeeFeedBack {
    @Id
    private String id;
    private String feedBackQuestion;
    private String feedBackAnswer;
    private String feedBackId;
    private String deleteStatus;
}
