package com.hepl.budgie.controller.preonboarding;

import com.hepl.budgie.config.i18n.Translator;
import com.hepl.budgie.config.security.JWTHelper;
import com.hepl.budgie.dto.GenericResponse;
import com.hepl.budgie.dto.preonboarding.*;
import com.hepl.budgie.entity.preonboarding.OnBoardingInfo;
import com.hepl.budgie.entity.userinfo.UserExpEducation;
import com.hepl.budgie.entity.userinfo.UserInfo;
import com.hepl.budgie.repository.preOnboardingRepository.OnboardingInfoRepository;
import com.hepl.budgie.repository.userinfo.UserExpEducationRepository;
import com.hepl.budgie.repository.userinfo.UserInfoRepository;
import com.hepl.budgie.service.PdfService;
import com.hepl.budgie.service.preonboarding.CandidateSeatingService;
import com.hepl.budgie.utils.AppMessages;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
//import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/seating")
@RequiredArgsConstructor
public class CandidateSeatingController {
    private final CandidateSeatingService candidateSeatingService;
    private final Translator translator;
    private final JWTHelper jwtHelper;
    private final UserExpEducationRepository userExpEducationRepository;
    private final UserInfoRepository userInfoRepository;
    private final OnboardingInfoRepository onboardingInfoRepository;
    private final PdfService pdfService;


    @GetMapping()
    public GenericResponse<Object> fetch(){
        List<SeatingRequestDTO> seatingRequestDTOS= candidateSeatingService.fetch();
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(seatingRequestDTOS)
                .build();

    }
    @PostMapping("/updateMultiple")
    public GenericResponse<List<OnBoardingInfo>> updateMultiple(@RequestBody List<EmployeeUpdateRequestDto> updateRequests) {
        List<OnBoardingInfo> updatedRecords = candidateSeatingService.update(updateRequests);

        if (updatedRecords.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }

        return GenericResponse.<List<OnBoardingInfo>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .build();
    }
    @GetMapping("/fetchApproved")
    public GenericResponse<Object> fetchApproved(){
        List<SeatingRequestDTO> seatingRequestDTOS= candidateSeatingService.fetchApprovedRequests();
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(seatingRequestDTOS)
                .build();
    }
    @PutMapping("/updatePreOnboarding")
    public GenericResponse<String> update(@Valid @RequestBody List<CandidateSeatingDto> updateRequests) {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        List<OnBoardingInfo> updatedRecords = candidateSeatingService.insertPreOnboarding(empId,updateRequests);
        return GenericResponse.success(translator.toLocale(AppMessages.CANDIDATE_UPDATED));


    }
    @GetMapping("/buddyEmployee")
    public GenericResponse<Object> buddyEmployee (){
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        BuddyDetailsDTO userInfo= candidateSeatingService.fetchBuddyDetails(empId);
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.PRE_ONBOARDING_DETAILS_FETCH))
                .errorType("NONE")
                .data(userInfo)
                .build();
    }
    @GetMapping("/master")
    public GenericResponse<Object> get(@RequestParam String referenceName ){
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String org = jwtHelper.getOrganizationCode();
        List <CandidateSeatingDto> masterFormSettings= candidateSeatingService.getAll(referenceName,org,empId);
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.BUDDY_FEEDBACK))
                .errorType("NONE")
                .data(masterFormSettings)
                .build();
    }

    @GetMapping("/buddy")
    public GenericResponse<Object> buddy(@RequestParam String referenceName){
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String org = jwtHelper.getOrganizationCode();

        BuddyDTO buddyDTO= candidateSeatingService.fetchBuddy(empId,referenceName,org);
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.BUDDY_FEEDBACK))
                .errorType("NONE")
                .data(buddyDTO)
                .build();

    }
    @PutMapping("updateBuddy")
    public  GenericResponse<String> updateBuddy(@RequestBody List<FeedbackFieldsDTO> buddyDTO,@RequestParam String referenceName){
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String org1 = jwtHelper.getOrganizationCode();
        candidateSeatingService.updateBuddyFeedback(empId,buddyDTO,referenceName,org1);
        return GenericResponse.success(translator.toLocale(AppMessages.BUDDY_FEEDBACK_UPDATE));
    }

    @GetMapping("/fetchInductionSchedule")
    public GenericResponse<Object> fetchInductionSchedule(@RequestParam String referenceName){
        String org1 = jwtHelper.getOrganizationCode();
        List<InductionScheduleDTO> inductionScheduleDTO= candidateSeatingService.fetchInductionSchedule(referenceName,org1);
        return GenericResponse.builder()
                .status(true)
                .message(translator.toLocale(AppMessages.INDUCTION_SCHEDULE_FETCH))
                .errorType("NONE")
                .data(inductionScheduleDTO)
                .build();
    }

    @GetMapping(value = "/userDetails", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<byte[]> getUserDetails() throws IOException {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String htmlContent = candidateSeatingService.fetchUserDetails(empId);

        byte[] pdfBytes = pdfService.generatePdf(htmlContent);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("filename", "UserDetails.pdf");

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

//    private void convertHtmlToPdf(String html, ByteArrayOutputStream outputStream) {
//        try {
//            ITextRenderer renderer = new ITextRenderer();
//            renderer.setDocumentFromString(html);
//
//            renderer.layout();
//            renderer.createPDF(outputStream, false);
//            renderer.finishPDF();
//
//        } catch (Exception e) {
//            throw new RuntimeException("PDF generation failed", e);
//        }
//    }

    @PutMapping("/updateInterestingFacts")
    public GenericResponse<String> updateInterestingFacts(
            @Valid @RequestBody InterestingFactsDTO interestingFactsDTO) {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String interest = candidateSeatingService.updateInterestingFacts(empId, interestingFactsDTO);

        return GenericResponse.<String>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.WELCOME_ABOARD))
                .errorType("NONE")
                .data(interest)
                .build();
    }



    @GetMapping("/welcomeAboard")
    public WelcomeAboardDTO getUserProfile() {
        String empId = jwtHelper.getUserRefDetail().getEmpId();
        String org = jwtHelper.getOrganizationCode();
        Optional<UserExpEducation> userExpEducationOpt = userExpEducationRepository.findByEmpId(empId);
        Optional<UserInfo> userInfoOpt = userInfoRepository.findByEmpId(empId);
        Optional<OnBoardingInfo> onBoardingInfoOpt = onboardingInfoRepository.findByEmpId(empId);
        if (userExpEducationOpt.isEmpty() && userInfoOpt.isEmpty() && onBoardingInfoOpt.isEmpty()) {
            return new WelcomeAboardDTO();
        }

        UserExpEducation userExpEducation = userExpEducationOpt.orElse(null);
        UserInfo userInfo = userInfoOpt.orElse(null);
        OnBoardingInfo onBoardingInfo = onBoardingInfoOpt.orElse(null);

        return candidateSeatingService.mapUserDetailsToDTO(userExpEducation, userInfo, onBoardingInfo);
    }

    @GetMapping("/interestingFact")
    public GenericResponse<List<InterestingFactsQuestionDTO>> interestingFactsQuestionDTOS(@RequestParam String referenceName) {
        String org = jwtHelper.getOrganizationCode();

        List<InterestingFactsQuestionDTO> interestingFactsQuestionDTOS = candidateSeatingService.fetchInterestingFacts(referenceName, org);

        return GenericResponse.<List<InterestingFactsQuestionDTO>>builder()
                .status(true)
                .message(translator.toLocale(AppMessages.INTERESTING))
                .errorType("NONE")
                .data(interestingFactsQuestionDTOS)
                .build();
    }





}

