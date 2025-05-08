package com.hepl.budgie.entity.organization;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "organisation")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Organization {

    @Id
    private String id;
    private String groupId;
    private String organizationDetail;
    private String organizationCode;
    private String email;
    private String industryType;
    private String tdsCircle;
    private String gstNumber;
    private String logo;
    private String contactNumber;
    private String address;
    private String country;
    private String iso3;
    private String state;
    private String town;
    private String pinOrZipcode;
    private String status;
    private List<Sequence> sequence;
    private String smtpProvider;
    private String smtpServer;
    private long smtpPort;
    private String fromMail;
    private String userName;
    private String password;
    private String signature;

}
