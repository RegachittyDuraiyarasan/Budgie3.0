package com.hepl.budgie.utils;

import com.hepl.budgie.entity.userinfo.Sections;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsUtil {
    private final UserInfoRepository userInfoRepository;

    public String getUserName(String empId) {
        return userInfoRepository.findByEmpId(empId)
                .map(userInfo -> {
                    Sections sections = userInfo.getSections();
                    return sections.getBasicDetails().getFirstName() + " " + sections.getBasicDetails().getLastName();
                })
                .orElse("");
    }
}
