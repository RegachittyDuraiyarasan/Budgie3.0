package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.ContactDTO;
import com.hepl.budgie.entity.userinfo.Contact;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.userinfo.ContactMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.service.userinfo.ContactService;
import com.hepl.budgie.utils.AppMessages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;


@Service
@Slf4j
public class ContactServiceImplementation implements ContactService {

    private final MasterFormService masterFormService;

    private final ContactMapper contactMapper;

    private final UserInfoRepository userInfoRepository;

    private final JWTHelper jwtHelper;

    private final MongoTemplate mongoTemplate;

    public ContactServiceImplementation(MasterFormService masterFormService, ContactMapper contactMapper,
                                        UserInfoRepository userInfoRepository, JWTHelper jwtHelper, MongoTemplate mongoTemplate) {
        this.masterFormService = masterFormService;
        this.contactMapper = contactMapper;
        this.userInfoRepository = userInfoRepository;
        this.jwtHelper = jwtHelper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public UserInfo updateContact(FormRequest formRequest, String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();
        log.info("User Info: {}", userInfo);

        Contact updateContact = contactMapper.toEntity(formRequest.getFormFields());

        if (Boolean.TRUE.equals(updateContact.getIsPermanentAddressDifferent())) {

            updateContact.getPresentAddressDetails().setPresentAddress(updateContact.getPermanentAddressDetails().getPermanentAddress());
            updateContact.getPresentAddressDetails().setPresentState(updateContact.getPermanentAddressDetails().getPermanentState());
            updateContact.getPresentAddressDetails().setPresentDistrict(updateContact.getPermanentAddressDetails().getPermanentDistrict());
            updateContact.getPresentAddressDetails().setPresentTown(updateContact.getPermanentAddressDetails().getPermanentTown());
            updateContact.getPresentAddressDetails().setPresentPinZipCode(updateContact.getPermanentAddressDetails().getPermanentPinZipCode());
        }

        Sections sections = userInfo.getSections();

        if (sections == null) {
            sections = new Sections();
        }

        sections.setContact(updateContact);
        userInfo.setSections(sections);

        userInfoRepository.save(userInfo);
        return userInfo;

    }

    @Override
    public ContactDTO getContact(String empId) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        Sections sections = userInfo.getSections();

        if (sections == null || sections.getContact() == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        Contact contactDetails = sections.getContact();

        ContactDTO contactDTO = contactMapper.mapToDTO(contactDetails);

        return contactDTO;
    }
}
