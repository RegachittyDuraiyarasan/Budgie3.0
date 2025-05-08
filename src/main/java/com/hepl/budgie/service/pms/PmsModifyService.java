package com.hepl.budgie.service.pms;

import com.hepl.budgie.dto.pms.PmsDTO;
import com.hepl.budgie.entity.pms.PmsListDTO;

import java.util.List;

public interface PmsModifyService {
    boolean addPms(PmsDTO request);
    Object fetchDataByLevel(PmsListDTO request);
    void updatePmsByLevel(List<PmsDTO> request);

}

