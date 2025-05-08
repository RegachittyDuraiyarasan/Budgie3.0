package com.hepl.budgie.service.impl.iiy;

import java.util.List;

import com.hepl.budgie.repository.iiy.IdeaCategoryRepository;
import com.hepl.budgie.utils.AppMessages;
import com.hepl.budgie.utils.AppUtils;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.iiy.IdeaCategory;
import com.hepl.budgie.service.iiy.IdeaCategoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.hepl.budgie.config.security.JWTHelper;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class IdeaCategoryServiceImpl implements IdeaCategoryService {
    private final IdeaCategoryRepository ideaCategoryRepository;
    private final MongoTemplate mongoTemplate;
    private final JWTHelper jwtHelper;

    @Override
    public void addIdeaCategory(IdeaCategory request) {
        log.info("Adding Idea Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = ideaCategoryRepository.existsByIdeaCategoryName(mongoTemplate, organizationCode,
                request.getIdeaCategoryName());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_IDEA_CATEGORY);

        request.setIdeaCategoryId(ideaCategoryRepository.findTopByOrderByIdDesc(organizationCode, mongoTemplate)
                .map(e -> AppUtils.generateUniqueId(e.getIdeaCategoryId()))
                .orElse("IDC000001"));
        request.setStatus(Status.ACTIVE.label);
        ideaCategoryRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Idea Category Saved Successfully");
    }

    @Override
    public List<IdeaCategory> fetchIdeaCategoryList() {
        log.info("Fetch Idea Category List");
        String organizationCode = jwtHelper.getOrganizationCode();
        return ideaCategoryRepository.findByAll(mongoTemplate, organizationCode);

    }

    @Override
    public void updateIdeaCategory(IdeaCategory request) {
        log.info("Updating Idea Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        boolean existStatus = ideaCategoryRepository.existsByIdeaCategoryNameAndIdeaCategoryIdNot(mongoTemplate,
                organizationCode, request.getIdeaCategoryName(), request.getIdeaCategoryId());
        if (existStatus)
            throw new ResponseStatusException(HttpStatus.CONFLICT, AppMessages.DUPLICATE_IDEA_CATEGORY);

        ideaCategoryRepository.findByIdeaCategoryId(mongoTemplate, organizationCode, request.getIdeaCategoryId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        ideaCategoryRepository.insertOrUpdate(request, mongoTemplate, organizationCode, authUser);
        log.info("Idea Category Updated Successfully");

    }

    @Override
    public void deleteIdeaCategory(String id) {
        log.info("Deleting Idea Category");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();
        ideaCategoryRepository.findByIdeaCategoryId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        ideaCategoryRepository.deleteByIdeaCategoryId(id, mongoTemplate, organizationCode, authUser);
        log.info("Idea Category Deleted Successfully");

    }

    @Override
    public void updateStatusIdeaCategory(String id) {
        log.info("Changing Idea Category Status");
        String organizationCode = jwtHelper.getOrganizationCode();
        String authUser = jwtHelper.getUserRefDetail().getEmpId();

        IdeaCategory IdeaCategory = (IdeaCategory) ideaCategoryRepository
                .findByIdeaCategoryId(mongoTemplate, organizationCode, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.ID_NOT_FOUND));
        String status = IdeaCategory.getStatus().equalsIgnoreCase(Status.ACTIVE.label) ? Status.INACTIVE.label
                : Status.ACTIVE.label;
        ideaCategoryRepository.updateByIdeaCategoryId(id, mongoTemplate, organizationCode, status, authUser);
        log.info("Idea Category Status Changed");

    }

    @Override
    public List<IdeaCategory> fetchIdeaCategory() {
        log.info("Fetch Active Idea Category List");
        String organizationCode = jwtHelper.getOrganizationCode();
        return ideaCategoryRepository.findAllByActiveStatus(mongoTemplate, organizationCode);
    }

}
