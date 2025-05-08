package com.hepl.budgie.service.gradeMaster;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.grade.GradeFetchDTO;
import com.hepl.budgie.entity.master.GradeMaster;

import java.util.List;

public interface GradeMasterService {
    GradeMaster addGrade(FormRequest formRequest, String org);

    List<GradeFetchDTO> getAllGrades(String org);

    GradeMaster updateGrade(FormRequest formRequest, String org, String gradeId);

    void deleteGrade(String gradeId,String org);

    void toggleStatus(String gradeId, String org);
}
