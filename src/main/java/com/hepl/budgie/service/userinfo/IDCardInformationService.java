package com.hepl.budgie.service.userinfo;

import com.google.zxing.WriterException;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.idCard.IdCardGenerationDto;
import com.hepl.budgie.entity.userinfo.UserInfo;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public interface IDCardInformationService {
    IdCardGenerationDto iDCardInformation(String empId);
    IdCardGenerationDto updateIdCard ( FormRequest form, UserInfo user) throws IOException , WriterException;
}
