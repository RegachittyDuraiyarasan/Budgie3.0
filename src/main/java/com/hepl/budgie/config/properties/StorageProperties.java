package com.hepl.budgie.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties("storage")
@Getter
@Setter
@Builder
public class StorageProperties {

	@Builder.Default
	private String location = "uploads";
	@Builder.Default
	private String organization = "organization";
	@Builder.Default
	private String recycle = "bin";
	@Builder.Default
	private String profile = "profile";
	@Builder.Default
	private String otherDocuments = "otherDocuments";
	@Builder.Default
	private String passportPhoto = "passportPhoto";
	@Builder.Default
	private String payslips = "payslips";
	@Builder.Default
	private String idCard = "idCard";
	@Builder.Default
	private String idCardGenerator = "idCardGenerator";
	@Builder.Default
	private String resume = "resume";
	@Builder.Default
	private String relievingLetter = "relievingLetter";
	@Builder.Default
	private String vaccination = "vaccination";
	@Builder.Default
	private String bankPassbook = "bankPassbook";
	@Builder.Default
	private String bloodGroupProof = "bloodGroupProof";
	@Builder.Default
	private String dateOfBirthProof = "dateOfBirthProof";
	@Builder.Default
	private String pan = "pan";
	@Builder.Default
	private String aadhaarCard = "aadhaarCard";
	@Builder.Default
	private String signature = "signature";
	@Builder.Default
	private String experience = "experience";
	@Builder.Default
	private String education = "education";
	@Builder.Default
	private String holiday = "holiday";
	@Builder.Default
	private String event = "event";
	@Builder.Default
	private String helpdesk = "helpdesk";
	@Builder.Default
	private String iiyCertificate = "iiyCertificate";
	@Builder.Default
	private String profilePhoto = "profilePhoto";
	@Builder.Default
	private String bannerImage = "bannerImage";
	@Builder.Default
	private String leaveApply = "leaveApply";
	@Builder.Default
	private String companyPolicy = "companyPolicy";
	@Builder.Default
	private String employeeDocument = "employeeDocument";
	@Builder.Default
	private String separation = "separation";
	@Builder.Default
	private String reimbursement = "reimbursement";
	@Builder.Default
	private String idCardPhoto = "idCardPhoto";
	@Builder.Default
	private String idCardPhotoHr = "idCardPhotoHr";

}
