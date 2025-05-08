package com.hepl.budgie.service.master;

import com.hepl.budgie.dto.form.FormRequest;

public interface OtherFormsActionsService {

    void saveDynamicData(FormRequest formRequest, String filter);

    void updateDynamicData(String id, FormRequest formRequest);
}
