package com.hepl.budgie.service.IdCard;

import com.hepl.budgie.dto.idCard.GraphicsTeamIdDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface IdCardPhotoService {
    String idCardUpload( String emp ,String empId , MultipartFile multipartFile) throws IOException;

    List<GraphicsTeamIdDTO>graphicsTeamIdDto(String empId ,
                                             String employeeName,
                                             String reportingManager,
                                             String dateOfJoining,
                                             String result);

    byte[] bulkUpload(String action, List<MultipartFile> files) throws IOException;
}
