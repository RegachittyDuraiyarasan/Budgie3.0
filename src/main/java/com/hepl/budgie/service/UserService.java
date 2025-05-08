package com.hepl.budgie.service;

import com.hepl.budgie.dto.UserDTO;
import com.hepl.budgie.entity.Users;

public interface UserService {

    Users saveUser(UserDTO users);

}