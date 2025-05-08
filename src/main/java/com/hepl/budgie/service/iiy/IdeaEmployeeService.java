package com.hepl.budgie.service.iiy;

import com.hepl.budgie.dto.iiy.IdeaEmployeeRequestDTO;
import com.hepl.budgie.dto.iiy.IdeaFetchDTO;

import java.util.List;

public interface IdeaEmployeeService {
    void addIdea(IdeaEmployeeRequestDTO data);

    List<IdeaFetchDTO> fetchIdeaList(IdeaEmployeeRequestDTO data);

    List<IdeaFetchDTO> fetchTeamIdeaList(IdeaEmployeeRequestDTO data);

    List<IdeaFetchDTO> fetchTeamIdeaReportList(IdeaEmployeeRequestDTO data);

    void approveTeamIdea(IdeaEmployeeRequestDTO data);

    void rejectTeamIdea(IdeaEmployeeRequestDTO data);
}
