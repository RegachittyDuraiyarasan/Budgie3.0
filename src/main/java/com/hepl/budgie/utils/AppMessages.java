package com.hepl.budgie.utils;

public class AppMessages {

    public static final String IMPORT_SUCCESS = "Employees imported successfully!";
    public static final String IMPORT_FAILURE = "Failed to import employees. Please check the file and try again.";
 

    private AppMessages() {
        throw new IllegalStateException("Utility class");
    }

    public static final String ACCESS_DENIED = "error.accessDenied";
    public static final String UNSUPPORTED_FORMAT = "error.unsupportedFormat";
    public static final String RESOURCE_NOT_FOUND = "error.resourceNotFound";
    public static final String FILE_NOT_FOUND = "error.fileNotFound";
    public static final String FOLDER_NOT_FOUND = "error.folderNotFound";
    public static final String FILE_TYPE_NOT_SUPPORTED = "error.fileNotSupported";
    public static final String MEETING_DRAFT_FROM = "error.canNotProceedMeeting";
    public static final String DOC_FORMAT_NOT_SUPPORTED = "error.docxFormatNotSupported";

    public static final String USER_SAVED = "user.userSavedSuccessfully";
    public static final String USER_UPDATED = "user.userUpdatedSuccessfully";
    public static final String USER_DELETED = "user.userDeletedSuccessfully";
    public static final String PASSWORD_CHANGED = "user.passwordChangedSuccessfully";
    public static final String ACCESS_CONTROL_UPDATE = "user.accessControlUpdatedSuccessfully";
    public static final String PASSWORD_LINK_SEND = "user.passwordLinkSendSuccessfully";
    public static final String USER_EMPTY_INDIVIDUAL = "user.userEmptyIndividual";
    public static final String USER_ID_ALREADY_EXISTS = "validation.user.userIdAlreadyExists";
    public static final String USERNAME_ALREADY_EXISTS = "validation.user.usernameAlreadyExists";
    public static final String MOBILE_ALREADY_EXISTS = "validation.user.mobileAlreadyExists";
    public static final String EMAIL_ALREADY_EXISTS = "validation.user.emailAlreadyExists";
    public static final String SCHEME_ALREADY_EXISTS = "error.schemeAlreadyExists";
    public static final String STATE_ALREADY_EXISTS = "error.stateAlreadyExists";
    public static final String SLAB_ALREADY_EXISTS = "error.slabAlreadyExists";
    public static final String ESIC_ACTIVE_CONFLICT = "error.esicActiveConflict";
    public static final String DOCUMENT_NOT_UPDATED = "error.failedtoupdatedocument";
    public static final String INVALID_HEADERS = "error.invalidHeaders";
    public static final String EMPTY_EXCEl = "error.emptyExcel";

    public static final String SUB_MENU_UNIQUE = "error.subMenuUnique";
    public static final String FILE_EXCEPTION = "error.fileCannotBeEmpty";
    public static final String INIT_STORAGE = "error.couldNotInitializeStorage";

    public static final String DATA_ENCRYPTED = "hashing.dataEncryptedSuccessfully";
    public static final String DATA_DECRYPTED = "hashing.dataDecryptedSuccessfully";

    public static final String FORM_SAVED = "form.formSavedSuccessfully";
    public static final String FORM_INVALID = "form.formInvalid";
    public static final String FORM_UPDATE = "form.formUpdatedSuccessfully";
    public static final String FORM_VALIDATED = "form.formValidatedSuccessfully";
    public static final String FORM_VALIDATION_FAILED = "form.formValidationFailed";
    public static final String FORM_FIELD_NOT_FOUND = "form.formFieldNotFound";
    public static final String FORM_FIELD_REQUIRED = "form.formFieldRequired";
    public static final String FORM_FIELD_DELETED = "form.formFieldDeletedSuccessfully";
    public static final String FORM_NUMBER_INVALID = "form.numberInvalid";
    public static final String FORM_EMAIL_INVALID = "form.emailInvalid";
    public static final String FORM_FILE_NOT_SUPPORTED = "form.fileFormatNotSupported";
    public static final String FORM_FILE_MAX_SIZE = "form.fileShouldBeMaximumOf";
    public static final String INVALID_PATTERN = "form.formInvalidPattern";
    public static final String PHONE_INVALID = "form.phoneNumNotValid";
    public static final String ALPHANUMERIC_INVALID = "form.alphaNumeric";
    public static final String ALPHA_INVALID = "form.alphaOnly";
    public static final String INVALID_FORM_FIELD = "form.invalidFormField";
    public static final String INVALID_TEXT = "form.invalidText";

    public static final String SETTINGS_OPTION_DELETED = "setting.optionDeleted";

    public static final String ORG_CREATED = "org.createdSuccessfully";

    public static final String IP_BLOCKED = "error.ipBlocked";
    public static final String TOO_MANY_REQUEST = "error.tooManyRequests";
    public static final String NO_DATA_FOUND = "error.noDataFound";

    public static final String DATA_UPDATED = "data.update";
    public static final String DATA_INSERTED = "data.update";
    public static final String ORG_ADDED = "org.add";
    public static final String ORG_DELETE = "org.delete";
    public static final String PAYROLL_ADD = "payroll.add";
    public static final String PAYROLL_UPDATE = "payroll.update";
    public static final String PAYROLL_DELETE = "payroll.delete";
    public static final String PAYROLL_STATUS = "payroll.status";
    public static final String REIMBURSEMENT_NOT_FOUND = "error.reimbursement.notFound";
    public static final String BILL_NO_ALREADY_EXIST = "error.billNo.alreadyExist";
    public static final String CTC_NOT_FOUND = "error.ctcNotFound";

    public static final String ORG_MAP_ADD = "org.orgMapAdded";
    public static final String PARENT_ORG_NOT_EXIST = "org.parentOrgNotExist";
    public static final String MULTI_CHILD_ORG_NOT_EXIST = "org.childOrgNotExist";
    public static final String MULTI_ORG_DUPLICATE = "org.orgAlreadyExist";
    public static final String LS_DELETE = "ls.delete";
    public static final String LEAVE_APPLIED = "leave.applied";
    public static final String LEAVE_UPDATED = "leave.update";

    public static final String PF_LOGIC_ADDED = "payroll.pflogicAdded";

    public static final String BASIC_DETAILS_UPDATE = "basicDetails.update";
    public static final String BASIC_DETAILS_FETCH = "basicDetails.fetch";

    public static final String HR_INFO_UPDATE = "hrInformation.update";

    public static final String OTHER_DOCUMENTS_UPDATE = "otherDocuments.update";
    public static final String FILE_VALIDATION = "documents.empty";

    public static final String EXPERIENCE_ID_NOT_FOUND = "experienceId.notFound";

    public static final String EXPERIENCE_ADDED = "experience.add";

    public static final String EXPERIENCE_DELETED = "experience.delete";

    public static final String EXPERIENCE_UPDATED = "experience.update";

    public static final String EXPERIENCE_PRESENT_ON = "experience.presentOn";

    public static final String EDUCATION_ID_NOT_FOUND = "educationId.notFound";

    public static final String EDUCATION_DELETED = "education.delete";

    public static final String EDUCATION_ADDED = "education.add";

    public static final String EDUCATION_UPDATED = "education.update";

    public static final String EDUCATION_NOT_FOUND = "education.notFound";

    public static final String FILE_UPLOAD = "file.upload";

    public static final String WORKING_INFORMATION_UPDATE = "workingInformation.update";

    public static final String CONTACT_UPDATE = "contact.update";

    public static final String ACCOUNT_INFORMATION_UPDATE = "accountInformation.update";

    public static final String FAMILY_UPDATE = "family.update";

    public static final String FAMILY_INSERT = "family.insert";

    public static final String FAMILY_DETAILS_NOT_FOUND = "familyDetails.notFound";

    public static final String FAMILY_ID_IS_REQUIRED = "familyId.isRequired";

    public static final String FAMILY_DELETE = "family.delete";

    public static final String EMERGENCY_CONTACT_NOT_FOUND = "emergencyContact.notFound";

    public static final String EMERGENCY_CONTACT_EXISTS = "emergencyContact.exists";

    public static final String FAMILY_ID_NOT_FOUND = "familyId.notFound";

    public static final String LEAVE_CODE_NOT_EXIST = "leave.notExist";
    public static final String WORKFLOW_SEQUENCE_MISMATCH = "workflow.sequenceOrderMismatch";

    public static final String EMPLOYEES_RETRIEVED_SUCCESS = "Employees retrieved successfully";
    public static final String EMPLOYEES_RETRIEVED_FILTERS = "emp.employeeRetrivedOnFilter";
    public static final String ERROR_OCCURED = "An error occurred. Please try again later.";
    public static final String EMPLOYEE_ACCESS_TYPE = "employee.accessTypeRequired";

    public static final String ERROR_STATE_EXIST = "error.state.exist";
    public static final String ID_NOT_FOUND = "error.idNotFound";
    public static final String EMP_NOT_FOUND = "error.empNotFound";
    public static final String HR_INFO_NOT_FOUND = "hrInfo.notFound";
    public static final String REPORTER_DETAIL_NOT_FOUND = "reporterDetail.notFound";
    public static final String WORKFLOW_NOT_FOUND = "workflow.notFound";

    public static final String PRE_ONBOARDING_DETAILS_FETCH = "preOnboarding.fetch";
    public static final String PRE_ONBOARDING_DOCUMENT_FETCH = "preOnboardingDocument.fetch";
    public static final String PRE_ONBOARDING_DOCUMENT_UPDATE = "preOnboardingDocument.update";
    public static final String PRE_ONBOARDING_UPDATE_EMPLOYEE_ID = "preOnboardingDocument.update";
    public static final String PRE_ONBOARDING_SEATING_FETCH = "preOnboardingSeating.fetch";

    public static final String INVOKE_METHOD_ERROR = "invokeMethod.error";

    public static final String POST_LEAVE_TRANSACTION = "postLeaveTransaction.update";
    public static final String PRE_ONBOARDING_STATUS_FETCH = "preOnboardingStatus.fetch";
    public static final String PRE_ONBOARDING_VERIFIED = "preOnboardingDocument.verified";
    public static final String ONBOARDING_INFO = "onboardingInfo.update";
    public static final String WORKFLOW_NOT_FOUND_FOR_ROLE = "workflow.notFound";
    public static final String ARGUEMENT_ID_REQUIRED = "workflow.argumentIdNotFount";
    public static final String ERROR_PROCESSING_ACTION = "workflow.errorProcessingAction";
    public static final String INSERT = "inserted.insert";
    public static final String UPDATE = "update.update";

    public static final String CANDIDATE_UPDATED = "CandidateSeating.update";
    public static final String BUDDY_FEEDBACK = "BuddyFeedBack.fetch";
    public static final String BUDDY_FEEDBACK_UPDATE = "BuddyFeedBack.update";
    public static final String INDUCTION_SCHEDULE_FETCH = "InductionSchedule.fetch";
    public static final String WELCOME_ABOARD = "WelcomeAboard.update";
    public static final String INTERESTING="Interesting.fetch";

    // workflow
    public static final String FUNCTION_EMPTY = "Function.empty";
    public static final String INVOKE_METHOD_NOT_FOUND = "InvokeMethod.notFound";
    public static final String MATCHING_METHOD_NOT_FOUND = "MatchingMethod.notFound";

    public static final String PEOPLE_STARRED = "people.star";

    public static final String PEOPLE_UNSTARRED = "people.unStar";

    public static final String PEOPLE_EMPTY = "people.empty";

    public static final String STARRED_ID_NOT_FOUND = "people.starIdNotFound";

    // template creation and bulk uploads
    public static final String EXCEL_TEMPLATE_CREATION_FAILED = "sample.templateCreationFailed";
    public static final String LEAVE_BALANCE_IMPORT = "upload.leaveBalanceImport";

    // leave management
    public static final String ADDED_LEAVE_TYPE_CATEGORY = "added.leaveTypeCategory";
    public static final String DELETED_LEAVE_TYPE_CATEGORY = "deleted.leaveTypeCategory";
    public static final String UPDATED_LEAVE_TYPE_CATEGORY = "updated.leaveTypeCategory";
    public static final String LEAVE_GRANT = "leave.grantSuccessfully";
    public static final String LEAVE_APPROVE = "leave.approve";
    public static final String LEAVE_REJECT = "leave.reject";
    public static final String LEAVE_WITHDRAWN = "leave.withdrawn";
    public static final String LEAVE_CANCEL = "leave.cancel";

    // attendance management
    public static final String ADDED_SHIFT_TYPE = "attendance.addedShiftType";
    public static final String UPDATED_SHIFT_TYPE = "attendance.updatedShiftType";
    public static final String DELETED_SHIFT_TYPE = "attendance.deletedShiftType";
    public static final String ADDED_SHIFT_MASTER = "attendance.addedShiftMaster";
    public static final String UPDATED_SHIFT_MASTER = "attendance.updatedShiftMaster";
    public static final String STATUS_CHANGED_SHIFT_MASTER = "attendance.statusChangedShiftMaster";
    public static final String ERROR_PROCESS_ACTION = "error.processAction";
    public static final String ORGANIZATION_ALREADY_MAPPED = "organization.alreadyMapped";

    public static final String MODULE_SETTINGS_ADDED = "moduleSettings.added";
    public static final String MODULE_SETTINGS_UPDATED = "moduleSettings.updated";
    public static final String MODULE_ID = "moduleSettings.moduleId";
    public static final String MODULE_SETTINGS_DELETED = "moduleSettings.deleted";
    // file upload in Organization
    public static final String FILE_UPLOAD_ERROR = "error.uploadfileError";
    public static final String ORG_ALREADY_EXITS = "org.alreadyExits";

    public static final String USER_NOT_FOUND = "user.userNotFound";
    public static final String USER_ONBOARDING = "user.onboarding";
    public static final String INVALID_FEEDBACK = "user.invalidFeedback";
    public static final String EMPLOYEE_NOT_FOUND = "user.employee";
    public static final String DOCUMENT_NOT_FOUND = "document.notFound";
    public static final String DOCUMENT_APPROVED = "document.approved";
    public static final String HR_SUGGESTEDMAIL = "hr.SuggestedMail";
    public static final String INVALID_MAIL_FORMAT = "user.invalidMailFormat";
    public static final String EMPLOYEEID_NOT_FOUND = "user.empIdNotFound";
    public static final String NEW_ONBOARDING = "user.newOnBoarding";
    public static final String ROLE_OF_INTAKE = "role.notFound";
    public static final String USER_OTHERDOCUMENT = "user.otherDocument";
    public static final String PERSONAL_EMAIL = "user.personalEmail";
    public static final String MASTER_SETTING_NOT_FOUND = "masterSetting.notFound";
    public static final String MASTER_FORM_SETTINGS = "masterFormSettings.notFound";
    public static final String ORGANIZATION_DETAILS_NOT_FOUND = "organizationDetails.notFound";
    public static final String ROLE_NOT_FOUND_IN_SEQUENCE = "roleAndSequence.notFound";
    public static final String USER_EXPERIENCE = "user.experience";
    public static final String REQUEST_NULL_OR_EMPTY = "request.nullOrEmpty";
    public static final String EMPLOYEE_ID_NOT_FOUND_IN_USER_TABLE = "user.empIdNotFoundInUserTable";
    public static final String UPDATED = "onboarding";
    // login
    public static final String DATA_FETCH_SUCCESS = "user.LoginSuccessful";
    public static final String INVALID_PASSWORD = "user.Invalidpassword";
    public static final String USER_IS_NOT_FOUND = "user.Usernotfound";
    public static final String PASSWORD_IS_CHANGED = "user.Passwordnotfound";
    public static final String EMPLOYEE_ROLE_REQUIRED = "user.EmployeeRoleIsMandatory";
    public static final String EMPLOYEE_ROLE_UPDATED = "user.EmployeeRoleUpdatedSuccessfully";
    public static final String ORG_ALREADY_MAPPED = "organization.addedAlready";
    public static final String ORG_NOT_FOUND = "organization.notFound";
    public static final String NAME_ALREADY_EXISTS = "settings.nameExists";
    public static final String MOVEMENT_INITIATED = "movement.initiate";
    public static final String MOVEMENT_NOT_FOUND = "movement.notFound";
    public static final String MOVEMENT_REV_UPDATE = "movement.revUpdate";
    public static final String EMPLOYEE_UNDER_RM = "employeesUnderRM.fetched";
    public static final String SUPERVISOR_OR_REVIEWER_NOT_MATCH = "supOrRev.notMatch";
    public static final String NOT_FOUND_SUB_ORG = "org.subOrgaNotFound";

    // IIY
    public static final String ADDED_COURSE_CATEGORY = "iiy.addedCourseCategory";
    public static final String UPDATED_COURSE_CATEGORY = "iiy.updatedCourseCategory";
    public static final String DELETED_COURSE_CATEGORY = "iiy.deletedCourseCategory";
    public static final String STATUS_CHANGED_COURSE_CATEGORY = "iiy.statusChangedCourseCategory";
    public static final String FETCH_COURSE_CATEGORY = "iiy.fetchCourseCategory";
    public static final String DUPLICATE_COURSE_CATEGORY = "iiy.duplicateCourseCategory";
    public static final String ADDED_COURSE = "iiy.addedCourse";
    public static final String UPDATED_COURSE = "iiy.updatedCourse";
    public static final String DELETED_COURSE = "iiy.deletedCourse";
    public static final String STATUS_CHANGED_COURSE = "iiy.statusChangedCourse";
    public static final String FETCH_COURSE = "iiy.fetchCourse";
    public static final String DUPLICATE_COURSE = "iiy.duplicateCourse";

    public static final String PROBATION = "probation.notFound";
    public static final String FEEDBACK_FORM_SAVED = "feedbackForm.saved";
    public static final String FEEDBACK_FORM_SUBMITTED = "feedbackForm.submitted";
    public static final String EXTENDED_MONTHS = "extendedMonths.required";
    public static final String EXTENDED_STATUS = "extendedStatus.required";
    public static final String INVALID_RESULTS = "invalid.results";
    public static final String HR_STATUS = "hrVerify.status";

    public static final String EMPLOYEE_ORG_UPDATED = "employee.org.updated";
    public static final String HOLIDAY_NOT_FOUND = "holiday.notFound";
    public static final String EVENT = "event.notFound";

    public static final String EMP_ID_INCORRECT = "user.empIdIncorrect";
    public static final String EMP_PASSWORD_INCORRECT = "user.passwordIncorrect";
    public static final String MENU_ADDED_SUCCESS = "menu.addedSuccessfully";
    public static final String MENU_UPDATED_SUCCESS = "menu.updatedSuccessfully";
    public static final String MENU_STATUS = "menu.status";
    public static final String SUB_MENU_STATUS = "subMenu.status";
    public static final String MENU_DELETED = "menu.delete";
    public static final String SUB_MENU_DELETED = "subMenu.delete";
    public static final String SUB_MENU_UPDATED_SUCCESS = "subMenu.updatedSuccessfully";
    public static final String MENU_ID = "menu.id";
    public static final String SUB_MENU_ID = "subMenu.id";
    public static final String MENU_NOT_FOUND = "menu.notFound";
    public static final String INVALID_SUB_MENU = "invalid.subMenu";

    public static final String SUBMENU_ADDED_SUCCESS = "submenu.addedSuccessfully";
    public static final String SUB_MENU_CANNOT_ADD = "submenu.cannotBeAdded";

    public static final String ROLE_ADDED = "role.addedSuccessfully";
    public static final String ROLE_UPDATED = "role.updatedSuccessfully";
    public static final String ROLE_MENU_STATUS = "role.menusStatusUpdated";
    public static final String ROLE_SUBMENU_STATUS = "role.subMenuStatusUpdated";

    public static final String ASS_REV_STATUS_CANT_UPDATE = "assignedRev.cannotUpdate";
    public static final String OFF_REV_MUST_APPROVE = "offRev.mustApprove";
    public static final String BOTH_REV_MUST_APPROVE = "bothRev.mustApprove";
    public static final String PROFILE_UPLOAD = "profile.uploadedSuccessfully";
    public static final String PROFILE_BANNER = "profile.bannerUploadedSuccessfully";
    public static final String ID_CARD = "idCard.upload";
    public static final String HOLIDAY = "holiday.alreadyExists";
    public static final String WEEKEND_POLICY_SAVED_SUCCESSFULLY = "attendance.weekendpolicysavedSuccessfully";
    public static final String WEEKEND_POLICY_UPDATED_SUCCESSFULLY = "attendance.weekendpolicyUpdatedSuccessfully";
    public static final String HR_MOVEMENT_UPDATE = "movement.hrUpdate";
    public static final String NOT_ALLOWED_TO_WITHDRAW = "notAllowed.withdraw";
    public static final String REASON = "reason.notFound";
    public static final String DAY_TYPE_HISTORY = "attendance.dayTypeHistorySavedSuccessfully";
    public static final String ADDED = "employee.add";
    public static final String LOGOUT_SUCCESS = "app.logout";
    public static final String FILE_IS_EMPTY = "error.fileEmpty";
    public static final String ATTENDANCE_MUSTER_SAVED = "attendance.attendanceMusterSavedSuccessfully";
    public static final String PMS_LEVEL_ADDED = "pmsLevel.added";
    public static final String ADDED_DOCUMENT_TYPE = "documentType.added";
    public static final String DOCUMENT_UPDATED = "documentinfo.Updated";
    public static final String DOCUMENT_DELETED = "documentinfo.Deleted";
    public static final String DOCUMENT_ALREADY_EXISTS = "documentinfo.exists";
    public static final String DOCUMENT_UPLOAD_ERROR = null;
    public static final String PMS_FLOW_UPDATED = "pmsLevel.flow";
    // Idea Catgory
    public static final String ADDED_IDEA_CATEGORY = "idea.addedIdeaCategory";
    public static final String UPDATED_IDEA_CATEGORY = "idea.updatedIdeaCategory";
    public static final String DELETED_IDEA_CATEGORY = "idea.deletedIdeaCategory";
    public static final String STATUS_CHANGED_IDEA_CATEGORY = "idea.statusChangedIdeaCategory";
    public static final String FETCH_IDEA_CATEGORY = "idea.fetchIdeaCategory";
    public static final String DUPLICATE_IDEA_CATEGORY = "idea.duplicateIdeaCategory";
    public static final String ADDED_ACTIVITY = "iiy.addedActivity";
    public static final String FETCH_ACTIVITY = "iiy.fetchActivity";
    public static final String FETCH_TEAM_ACTIVITY = "iiy.fetchTeamActivity";
    public static final String FETCH_TEAM_ACTIVITY_REPORT = "iiy.fetchTeamActivityReport";
    public static final String APPROVE_ACTIVITY = "iiy.approvedActivity";
    public static final String REJECT_ACTIVITY = "iiy.rejectedActivity";
    public static final String FETCH_OVERALL_ACTIVITY_REPORT = "iiy.fetchOverallActivityReport";
    public static final String FETCH_IIY_REPORT = "iiy.fetchIIYReport";
    public static final String FETCH_IIY_PROCESSING_COURSE = "iiy.fetchIIYProcessingCourse";
    public static final String FETCH_IIY_EMPLOYEE_REPORT = "iiy.fetchIIYEmployeeReport";
    public static final String ADDED_IDEA = "idea.addedIdea";
    public static final String FETCH_IDEA = "idea.fetchIdea";
    public static final String FETCH_TEAM_IDEA = "idea.fetchTeamIdea";
    public static final String FETCH_TEAM_IDEA_REPORT = "idea.fetchTeamIdeaReport";
    public static final String APPROVE_IDEA = "idea.approvedIdea";
    public static final String REJECT_IDEA = "idea.rejectedIdea";
    public static final String FETCH_OVERALL_IDEA_REPORT = "idea.fetchOverallIdeaReport";
    public static final String MOVEMENT_WITHDRAW = "movement.withdraw";
    public static final String ATTENDANCE_NOT_FOUND = "attendance.notFound";
    public static final String PMS_LEVEL_TIME = "pms.levelTimeUpdate";
    public static final String PMS_LEVEL_DETAILS_UPDATED = "pms.levelDetailsUpdate";
    public static final String DAY_TYPE_HISTORY_IMPORT = "attendance.dayTypeHistoryImport";
    public static final String PMS_APPLIED = "pms.EmpApplied";
    public static final String KEY_DRIVERS_NOT_MATCH = "pms.keyNotMatch";
    public static final String PMS_YEAR_NOT_FOUND = "pms.pmsYearNotFound";
    public static final String LEVEL_NOT_FOUND = "pms.levelNotFound";
    public static final String ACTION_NOT_FOUND = "pmsLevel.actionNotFound";
    public static final String NEXT_LEVEL_NOT_FOUND = "pmsLevel.nextLevelNotFound";
    public static final String SHIFT_ROSTER_IMPORT = "shiftRoster.import";
    public static final String PMS_ALREADY_SAVED = "pms.alreadySaved";
    public static final String NO_BUSINESS_HEAD = "org.businessHead";

    public static final String ATTENDANCE_SIGNED_IN = "attendance.attendanceSignInSuccessfully";
    public static final String ATTENDANCE_SIGNED_OUT = "attendance.attendanceSignOutSuccessfully";
    public static final String PLEASE_SIGN_IN_FIRST = "attendance.pleaseSignInFirst";
    public static final String FAILED_TO_DOWNLOAD = "attendance.failedToDownload";
    public static final String INVALID_DATE_TIME = "error.invalidDateFormat";
    public static final String ATTENDANCE_BULK_UPLOAD = "attendance.attendanceBulkUpload";
    public static final String EMPLOYEE_NOT_REVIEWER = "attendance.employeeNotReviewer";
    public static final String PAYROLL_LOCK_NOT_FOUND = "attendance.payrollLockNotFound";
    public static final String FINANCIAL_YEAR_UPDATE = "financialYear.update";
    public static final String FBP_ALREADY_DECLARED = "fbp.already.declared";
    public static final String FBP_HR_RELEASE = "fbp.hr.release";
    public static final String FBP_RANGE_NOT_FOUND = "fbp.range.notFound";
    public static final String FBP_COMPONENT_RANGE_NOT_FOUND = "fbp.component.range.notFound";


    public static final String PMS_DATA_NOT_FOUND = "pms.dataNotFound";
    public static final String PMS_UPDATED = "pms.update";
    public static final String PMS_RM_UNAUTHORIZED = "pms.rmUnauthorized";

    public static final String NO_DATA = "no.data";
    // document center
    public static final String DOCUMENT_ADDED_SUCCESSFULLY = "documentCenter.added";
    public static final String UNAUTHORIZED_ACTIVITY = "iiy.unauthorizedActivity";

    public static final String DOCUMENT_REPORT_ADDED = "documentCenterReport.added";
    public static final String DOCUMENT_STATUS_UPDATED = "documentCenter.statusUpdate";
    public static final String DOCUMENT_ID_REQUIRED = "documentCenter.documentIdRequired";
    public static final String DUPLICATE_DOCUMENT_TYPES = "documentCenter.documentTypeDuplicate";
    public static final String EMPLOYEE_DOCUMENT_NOT_FOUND = "documentCenter.employeeDocumentNotFound";
    public static final String INVALID_FILE_FORMAT = "documentCenter.fileformateinvalid";
    public static final String NO_FILES_UPLOADED_FOR_BULK_UPLOAD = "documentCenter.noFilesUploadedForBulkUpload";
    public static final String INVALID_FILE_NAME_FORMAT = "documentCenter.invalidFileNameFormat";
    public static final String ONE_OF_THE_UPLOADED_FILES_IS_EMPTY = "documentCenter.oneOfTheUploadedFilesIsEmpty";
    public static final String FILENAME_MUST_MATCH_EMPID = "documentCenter.fileNameMustMatchEmpId";
    public static final String INVALID_FILE_FORMAT_PDF_ONLY = "documentCenter.invalidFileFormatPdfOnly";
    public static final String FILENAME_MISMATCH = "documentCenter.fileNameMisMatch";
    public static final String EMPLOYEE_ID_NOT_FOUND = "documentCenter.employeeIdNotFound";


    public static final String GENERATE_MONTH = "payroll.month.generate";
    public static final String ATTENDANCE_DATE_NOT_FOUND = "attendance.date.not.update";
    public static final String PAYROLL_MONTH_NOT_FOUND = "payroll.month.not.found";
    public static final String COMPONENT_ID_NOT_FOUND = "component.id.not.found";
    public static final String COMPONENT_ID_ALREADY_FOUND = "component.already.found";
    public static final String COUNTRY_ALREADY_FOUND = "country.already.found";
    public static final String PAYSHEET_RUN_EMPLOYEE_COUNT = "payroll.paysheet.run.count";
    public static final String ATTENDANCE_DAY_TYPE_HISTORY = "attendance.dayTypeHistory";
    public static final String FILE_NOT_FOUND_DATE = "attendance.fileNotFoundForDate";
    public static final String ATTENDANCE_OVERRIDE = "attendance.overrideSuccessfully";

    public static final String GRADE_ADDED = "grade.added";
    public static final String GRADE_ID_REQUIRED = "gradeId.required";
    public static final String GRADE_ID_NOTFOUND = "gradeId.notFound";
    public static final String GRADE_UPDATED = "grade.updated";
    public static final String GRADE_DELETED = "grade.deleted";
    public static final String GRADE_STATUS = "grade.status";

    public static final String EMP_ROLE_MUST = "editRole.empMust";
    public static final String COMP_OFF_APPLIED = "compOff.applied";
   

    // company policy 
    public static final String COMPANY_POLICY_ADDED_SUCCESSFULLY="policy.addedSuccessfully";
    public static final String POLICY_CATEGORY_NOT_FOUND ="policy.policyCategoryNotFound";
    public static final String UNKNOWN_STATUS = "policy.unknownStatus";
    public static final String NOT_FOUND_IN_POLICY_CATEGORY ="policy.notFoundInPolicyCategory";
    public static final String STATUS_UPDATED_SUCCESSFULLY = "policy.statusUpdatedSuccessfully";
    public static final String POLICY_DELETED_SUCCESSFULLY ="policy.deteledSuccessfully";
    public static final String COMPANY_POLICY_UPDATED_SUCCESSFULLY ="policy.companyPolicyUpdatedSuccessfully";

    public static final String PAYROLL_IT_SECTION_SAVED = "payroll.itSectionSavedSuccessfully";
    public static final String PAYROLL_TYPE_EXISTS = "payroll.typeAlreadyExists";
	public static final String PAYROLL_IT_SCHEME_SAVED = "payroll.itSchemeSavedSuccessfully";
    public static final String PAYROLL_TYPE_NOT_FOUND = "payroll.typenotFound";
    public static final String PAYROLL_SCHEME_EXISTS = "payroll.schemeAlreadyExists";

    public static final String PAYROLL_IT_DECLARATION_CREATED = "payroll.itDeclarationCreated";
    public static final String IT_DECLARATION_PAST_DATE = "payroll.itDeclarationPastDate";
    public static final String REGIME_UPDATED = "payroll.regimeUpdated";
    public static final String SCHEMES_UPDATED = "payroll.itDeclarationSchemesUpdated";
    public static final String PAYROLL_SCHEME_NOT_FOUND = "payroll.schemeNotFound";
    public static final String HRA_UPDATED = "payroll.hraUpdated";
    public static final String PAYROLL_HRA_DATE_ERROR = "payroll.hraDateError";
    public static final String METRO_UPDATED = "payroll.metroUpdated";
    public static final String PREVIOUS_EMPLOYEE_UPDATED = "payroll.previousEmployeeUpdated";
    public static final String NOT_ELIGIBLE_FOR_PREVIOUS_EMPLOYEE = "payroll.notEligibleForPreviousEmployee";
    public static final String FAMILY_UPDATED = "payroll.familyUpdated";

    public static final String INVALID_ID_CARD = "invalid.idCard";
    public static final String FILE_EXISTS = "file.exists";
    public static final String STATUS_NOT_ACTIVE = "status.notActive";
    public static final String NO_FILES_UPLOAD = "no.files";

    // employeeList
    public static final String PASSWORD_NOT_EMPTY ="user.passwordNotEmpty";
    public static final String PASSWORD_MUST_BE = "user.passwordLength";
    public static final String PASSWORD_NOT_MATCH ="user.passwordMismatch";
    public static final String PASSWORD_UPDATED_SUCCESSFULLY ="user.passwordUpdatedSuccessfully";
    public static final String LET_OUT_UPDATED = "payroll.letOutUpdated";


    public static final String SPOC_ADDED = "helpdesk.add";
    public static final String ALREADY_SIGNED_IN = "alreadySignedIn";
   
}
