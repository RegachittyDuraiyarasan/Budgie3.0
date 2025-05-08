package com.hepl.budgie.dto.leave;

import java.util.List;

import com.hepl.budgie.entity.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompOffDto {
    
    @Schema(example = "yyyy-MM-dd")
    private String compOffDate;
    @Schema(example = "yyyy-MM-dd")
    private String workedDate;
    @Schema(example = "yyyy-MM-dd")
    private String date;
    private Status fromSession;
    private Status toSession;
    private String appliedTo;
    private List<String> cc;
    private String reason;
    
}
