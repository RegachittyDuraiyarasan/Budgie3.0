package com.hepl.budgie.entity;

import java.util.HashMap;
import java.util.Map;

public enum FileType {
	ID_CARD("IdCardServiceImpl", "ID_CARD"),
	ID_CARD_GENERATOR("IdCardGenerationServiceImpl", "ID_CARD_GENERATOR"),
	PROFILE("ProfileFileServiceImpl", "PROFILE"),
	ORGANISATION("OrganisationFileServiceImpl", "ORGANISATION"),
	PASSPORT_PHOTO("PassportFileServiceImpl", "PASSPORT_PHOTO"),
	PAYSLIPS("PayslipsFileServiceImpl", "PAYSLIPS"),
	RESUME("ResumeFileServiceImpl", "RESUME"),
	RELIEVING_LETTER("RelievingLetterFileServiceImpl", "RELIEVING_LETTER"),
	VACCINATION("VaccinationFileServiceImpl", "VACCINATION"),
	BANK_PASSBOOK("PassbookFileServiceImpl", "BANK_PASSBOOK"),
	BLOOD_GROUP_PROOF("BloodGroupFileServiceImpl", "BLOOD_GROUP_PROOF"),
	DATE_OF_BIRTH_PROOF("DateOfBirthFileServiceImpl", "DATE_OF_BIRTH_PROOF"),
	PAN("PanFileServiceImpl", "PAN"),
	AADHAAR_CARD("AadhaarCardFileServiceImpl", "AADHAAR_CARD"),
	SIGNATURE("SignatureFileServiceImpl", "SIGNATURE"),
	EXPERIENCE("ExperienceFileServiceImpl", "EXPERIENCE"),
	EDUCATION("EducationFileServiceImpl", "EDUCATION"),
	BIN("BinFileServiceImpl", "BIN"),
	USERID_CARD("ResponseUserDetailsServiceImpl", "USERID_CARD"),
	HOLIDAY("HolidayFileServiceImpl", "HOLIDAY"),
	HELPDESK("HelpDeskFileServiceImpl", "HELPDESK"),
	PROFILE_PHOTO("ProfilePhotoFileServiceImpl", "PROFILE_PHOTO"),
	ID_CARD_PHOTO("IdCardPhotoFileServiceImpl", "ID_CARD_PHOTO"),
	EMPLOYEE_DOCUMENT("EmployeeDocumentFileServiceImp", "EMPLOYEE_DOCUMENT"),
	BANNER_IMAGE("BannerImageFileServiceImpl", "BANNER_IMAGE"),
	COMPANY_POLICY("CompanyPolicyServiceImplementation","COMPANY_POLICY"),
	EVENTS("EventFileServiceImpl","EVENTS"),
	REIMBURSEMENT("ReimbursementFileService","REIMBURSEMENT"),
	LEAVE_APPLY("LeaveApplyImpl", "LEAVE_APPLY"),
	IIY_CERTIFICATE("IIYFileServiceImpl", "IIY_CERTIFICATE"),
	SEPARATION("SeparationFileServiceImpl", "SEPARATION"),
	Id_CARD_BY_HR("IdCardByHRServiceImpl", "ID_CARD_BY_HR");

	public final String label;
	public final String folderName;
	private static final Map<String, FileType> BY_LABEL = new HashMap<>();

	static {
		for (FileType e : values()) {
			BY_LABEL.put(e.folderName, e);
		}
	}

	private FileType(String label, String folderName) {
		this.label = label;
		this.folderName = folderName;
	}

	public static FileType valueOfFolderName(String folderName) {
		return BY_LABEL.get(folderName);
	}
}