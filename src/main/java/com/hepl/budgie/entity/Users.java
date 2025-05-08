package com.hepl.budgie.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document("users")
@Data
@AllArgsConstructor
@NoArgsConstructor
// @EqualsAndHashCode(callSuper = true)
public class Users {

    @Id
    private String id;
    private String userId;
    private String username;
    private String mobile;
    private String email;
    private String roleId;
    private String password;
    private String profilePhotoPath;
    private LocalDateTime deletedDate;
    private Status status = Status.ACTIVE;

}
