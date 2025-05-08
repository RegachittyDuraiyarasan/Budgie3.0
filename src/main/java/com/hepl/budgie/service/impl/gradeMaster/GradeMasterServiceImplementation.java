package com.hepl.budgie.service.impl.gradeMaster;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.grade.GradeFetchDTO;
import com.hepl.budgie.entity.Status;
import com.hepl.budgie.entity.master.GradeMaster;
import com.hepl.budgie.entity.master.ProbationDetail;
import com.hepl.budgie.mapper.gradeMaster.GradeMapper;
import com.hepl.budgie.repository.gradeMaster.GradeMasterRepository;
import com.hepl.budgie.service.gradeMaster.GradeMasterService;
import com.hepl.budgie.utils.AppMessages;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GradeMasterServiceImplementation implements GradeMasterService {

    private final GradeMasterRepository gradeMasterRepository;

    private final MongoTemplate mongoTemplate;

    private final GradeMapper gradeMapper;

    @Override
    public GradeMaster addGrade(FormRequest formRequest, String org) {
        GradeMaster newGradeMaster = gradeMapper.toEntity(formRequest.getFormFields());

        // Extract probationStatus from the formFields map
        Object probationStatusObj = formRequest.getFormFields().get("probationStatus");
        Boolean probationStatus = false;

        if (probationStatusObj instanceof Boolean) {
            probationStatus = (Boolean) probationStatusObj;
        } else if (probationStatusObj instanceof String) {
            probationStatus = Boolean.parseBoolean((String) probationStatusObj);
        }

        // If probationStatus is false, clear extendedOptionMonth and set defaultDurationMonth to 0
        if (Boolean.FALSE.equals(probationStatus) && newGradeMaster.getProbationDetail() != null) {
            ProbationDetail probationDetail = newGradeMaster.getProbationDetail();
            probationDetail.setExtendedOptionMonth(Collections.emptyList());
            probationDetail.setDefaultDurationMonth(0);
        }

        log.info("newGradeMaster: {}", newGradeMaster);
        gradeMasterRepository.saveGrade(newGradeMaster, org, mongoTemplate);
        return newGradeMaster;
    }

    @Override
    public List<GradeFetchDTO> getAllGrades(String org) {
        String collectionName = gradeMasterRepository.getCollectionName(org);

        if (!mongoTemplate.collectionExists(collectionName)) {
            return Collections.emptyList();
        }

        Query query = new Query()
                .addCriteria(Criteria.where("status").ne(Status.DELETED.label));

        List<GradeMaster> gradeMasters =
                mongoTemplate.find(query, GradeMaster.class, collectionName);

        return gradeMasters.stream()
                .map(gradeMapper::mapToDTO)
                .collect(Collectors.toList());
    }


    @Override
    public GradeMaster updateGrade(FormRequest formRequest, String org, String gradeId) {
        GradeMaster existingGrade = gradeMasterRepository.findByGradeId(gradeId, org, mongoTemplate)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.GRADE_ID_NOTFOUND));

        Map<String, Object> updateFields = formRequest.getFormFields();
        log.info("updateFields {} ", updateFields);

        if (!Status.ACTIVE.label.equalsIgnoreCase(existingGrade.getStatus())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.GRADE_ID_NOTFOUND );
        }

        GradeMaster updatedEntity = gradeMapper.toEntity(updateFields);
        log.info("updatedEntity {}",updatedEntity);
        // Now merge updated fields into existingGrade manually
        if (updatedEntity.getGradeName() != null) existingGrade.setGradeName(updatedEntity.getGradeName());
        if (updatedEntity.getNoticePeriod() != null) existingGrade.setNoticePeriod(updatedEntity.getNoticePeriod());
        if (updatedEntity.getStatus() != null) existingGrade.setStatus(updatedEntity.getStatus());
        if (updatedEntity.getProbationStatus() != null) existingGrade.setProbationStatus(updatedEntity.getProbationStatus());
        if (updatedEntity.getProbationDetail() != null) existingGrade.setProbationDetail(updatedEntity.getProbationDetail());

        if (Boolean.FALSE.equals(existingGrade.getProbationStatus()) && existingGrade.getProbationDetail() != null) {
            ProbationDetail probationDetail = existingGrade.getProbationDetail();
            probationDetail.setDefaultDurationMonth(0);
            probationDetail.setExtendedOptionMonth(Collections.emptyList());
        }

        gradeMasterRepository.updateGradeOption(existingGrade, org, mongoTemplate);

        return existingGrade;
    }

    @Override
    public void deleteGrade(String gradeId, String org){
        UpdateResult result = gradeMasterRepository.deleteOptions(gradeId, mongoTemplate, org);
        log.info("result {}",result);
        if (result.getMatchedCount() == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, AppMessages.RESOURCE_NOT_FOUND);
        }
    }

    @Override
    public void toggleStatus(String gradeId, String org) {
        gradeMasterRepository.gradeStatus(gradeId, mongoTemplate, org);
    }



}
