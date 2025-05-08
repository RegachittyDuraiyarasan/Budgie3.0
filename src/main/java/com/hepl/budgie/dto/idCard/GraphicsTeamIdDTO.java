package com.hepl.budgie.dto.idCard;

import com.hepl.budgie.dto.userinfo.ImageDTO;
import lombok.Data;

@Data
public class GraphicsTeamIdDTO {
    private String employeeCode;
    private String reportingManager;
    private String dateOfJoining;
    private ImageDTO viewPassPortPhoto;
    private ImageDTO downloadPassPortPhoto;
    private ImageDTO idCardPhoto;
    private String photoStatusByGraphics;
}
