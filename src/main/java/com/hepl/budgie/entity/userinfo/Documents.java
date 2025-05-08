package com.hepl.budgie.entity.userinfo;

import lombok.Data;

import java.util.List;

@Data
public class Documents {
    private PassportPhoto passportPhoto;
    private PassportPhoto resume;
    private List<PassportPhoto> payslips;
    private PassportPhoto relievingLetter;
    private PassportPhoto vaccination;
    private PassportPhoto bankPassbook;
    private PassportPhoto bloodGroupProof;
    private PassportPhoto dateOfBirthProof;
    private PassportPhoto pan;
    private PassportPhoto aadhaarCard;
    private PassportPhoto signature;
    private List<PassportPhoto> compensationRevision;

}
