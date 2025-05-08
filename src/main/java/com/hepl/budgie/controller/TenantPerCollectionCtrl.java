package com.hepl.budgie.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.form.AccessLevelType;
import com.hepl.budgie.dto.form.FormDTO;
import com.hepl.budgie.entity.master.MasterFormOptions;
import com.hepl.budgie.entity.userinfo.BankDetails;
import com.hepl.budgie.entity.userinfo.BankDetailsEnc;
import com.hepl.budgie.mapper.BankMapper;
import com.hepl.budgie.repository.BankDetailsEncRepository;
import com.hepl.budgie.service.KMSHandlerService;
import com.hepl.budgie.service.master.MasterFormService;
import com.hepl.budgie.utils.AppMessages;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Create and Manage Tenant per collection", description = "")
@RestController
@RequestMapping("/test/tenant-per")
@RequiredArgsConstructor
@Slf4j
public class TenantPerCollectionCtrl {

    private final MasterFormService masterFormService;
    private final BankMapper bankMapper;
    private final KMSHandlerService kmsHandlerService;
    private final BankDetailsEncRepository bankDetailsEncRepository;
    private final Translator translator;

    @GetMapping("/test")
    public GenericResponse<String> getMethodName(@RequestParam String param) {
        return GenericResponse.success(translator.toLocale("test.icu", new Object[] { "GH", "Alice", 1 }));
    }

    @GetMapping("/category")
    public GenericResponse<List<MasterFormOptions>> getCategory() {

        List<MasterFormOptions> options = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            MasterFormOptions option = new MasterFormOptions();
            option.setName(String.valueOf(i));
            option.setValue(String.valueOf(i));
            options.add(option);
        }

        return GenericResponse.success(options);
    }

    @GetMapping("/subcategory")
    public GenericResponse<List<MasterFormOptions>> getSubCategory(
            @RequestParam(defaultValue = "", required = false) String category) {

        List<MasterFormOptions> options = new ArrayList<>();
        if (!category.isEmpty()) {
            for (int i = 0; i < 2; i++) {
                MasterFormOptions option = new MasterFormOptions();
                option.setName(String.valueOf(i));
                option.setValue(String.valueOf(i));
                options.add(option);
            }
        }

        return GenericResponse.success(options);
    }

    @PostMapping()
    @Operation(summary = "Submit Form")
    public GenericResponse<String> saveForm(@Valid @RequestBody FormDTO formDTO, @RequestParam String org) {
        log.info("Save forms - {}", formDTO.getFormName());
        masterFormService.saveForm(formDTO, "_" + org);

        return GenericResponse.success(translator.toLocale(AppMessages.FORM_SAVED));
    }

    @GetMapping()
    @Operation(summary = "Get forms based on form name")
    public GenericResponse<FormDTO> fetch(@RequestParam String formName, @RequestParam String org) {
        log.info("Get forms - {}", formName);

        BankDetails details = new BankDetails();
        details.setAccountNumber("12x3444");
        details.setBankName("SBI3");

        BankDetailsEnc detailsEnc = bankMapper.eBankDetailsEnc(details, kmsHandlerService);
        bankDetailsEncRepository.save(detailsEnc);

        BankDetails decDetails = bankMapper.toBankDetailsDecrypted(detailsEnc, kmsHandlerService);
        log.info("Account no. {}", decDetails.getAccountNumber());

        // List<BankDetails> bankDetails = mongoTemplate.findAll(BankDetailsEnc.class,
        // "bankDetails").stream()
        // .map(e -> bankEntityHelper.getPerson(e)).toList();

        return GenericResponse.success(masterFormService.getFormByName(formName, AccessLevelType.ADD));
    }

    @PostMapping("/test")
    @Operation(summary = "Get settings based on reference name")
    public GenericResponse<String> fetch(@RequestParam String filter)
            throws JsonProcessingException {
        log.info("Settings reference Name - {}");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = mapper.readValue(filter, Map.class);

        return GenericResponse.success("");
    }

}
