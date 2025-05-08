package com.hepl.budgie.service.master;

import com.hepl.budgie.dto.form.FormRequest;
import com.hepl.budgie.dto.form.OptionsResponseDTO;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.settings.MasterFormSettings;

import java.util.List;
import java.util.Map;

public interface MasterSettingsService {

    List<MasterFormOptions> getSettingsByReferenceName(String referenceName);

    MasterFormSettings addOrUpdateSettings(FormRequest formRequest, String referenceName);

    // void initializeMasterSettings(List<String> masters, Map<String, List<String>> predefinedMasters,
    //         String organisation);

    MasterFormSettings updateSettings(String value, String referenceName, FormRequest formRequest);

    void deleteOptions(String value, String referenceName);

    void initIndexingSettingForOrganisation(String organisation);

    List<OptionsResponseDTO> fetchOptions(String org);

    MasterFormSettings updateStatusSettings(String value, String referenceName, String status);
}
