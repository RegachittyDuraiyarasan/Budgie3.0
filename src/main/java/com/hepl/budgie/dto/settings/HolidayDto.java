package com.hepl.budgie.dto.settings;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.hepl.budgie.entity.Status;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HolidayDto {
    @Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private String id;
    private String occasion;
    private Status restrictedHoliday;
    private boolean allState;
    private boolean allLocation;
    private List<String> stateList;
    private List<String> locationList;
    private String discription;
    private MultipartFile file;
    @Schema(accessMode = Schema.AccessMode.READ_WRITE, example = "yyyy-MM-dd")
    private String date;
}
