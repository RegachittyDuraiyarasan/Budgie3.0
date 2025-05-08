package com.hepl.budgie.service.impl.userinfo;

import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.userinfo.AccountInformationDTO;
import com.hepl.budgie.entity.userinfo.AccountInformation;
import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.mapper.userinfo.AccountInformationMapper;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.userinfo.AccountInformationService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountInformationServiceImplementation implements AccountInformationService {

    private final MasterFormService masterFormService;

    private final AccountInformationMapper accountInformationMapper;

    private final UserInfoRepository userInfoRepository;

    private final JWTHelper jwtHelper;

    @Override
    public UserInfo updateAccountInformation(FormRequest formRequest, String empId) {
        Optional<UserInfo> userInfoOptional = userInfoRepository.findByEmpId(empId);

        if (userInfoOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }

        UserInfo userInfo = userInfoOptional.get();

        AccountInformation updateAccountInformation = accountInformationMapper.toEntity(formRequest);

        Sections sections = userInfo.getSections();

        if (sections == null) {
            sections = new Sections();
        }

        sections.setAccountInformation(updateAccountInformation);
        userInfo.setSections(sections);

        userInfoRepository.save(userInfo);
        return userInfo;
    }

    @Override
    public AccountInformationDTO getAccountInformation(String empId) {
        UserInfo userInfo = userInfoRepository.findByEmpId(empId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND));

        Sections sections = userInfo.getSections();

        if (sections == null || sections.getAccountInformation() == null) {
            throw new ResponseStatusException(HttpStatus.ACCEPTED, "");
        }

        AccountInformation accountInformation = sections.getAccountInformation();

        AccountInformationDTO accountInformationDTO = accountInformationMapper.mapToDTO(accountInformation);

        return accountInformationDTO;
    }
}
