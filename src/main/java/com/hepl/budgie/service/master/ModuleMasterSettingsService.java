package com.hepl.budgie.service.master;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.entity.master.ModuleMaster;

import java.util.List;

public interface ModuleMasterSettingsService {

    void addModuleMaster(FormRequest formRequest, String referenceName, String org);

    List<ModuleMaster> getOptionsByReferenceName(String referenceName, String org);

    ModuleMaster updateModuleMaster(FormRequest formRequest, String referenceName, String org, String moduleId);

    void deleteOptions(String moduleId, String referenceName);

    void initIndexingModuleMasterSettings( String organisation);


}
