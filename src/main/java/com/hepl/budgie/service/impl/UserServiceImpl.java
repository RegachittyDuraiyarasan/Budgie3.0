package com.hepl.budgie.service.impl;

import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.UserDTO;
import com.hepl.budgie.entity.Users;
import com.hepl.budgie.mapper.UserMapper;
import com.hepl.budgie.repository.ma.UserMaRepository;
import com.hepl.budgie.service.UserService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserMaRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserMaRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Users saveUser(UserDTO users) {
        log.info("User service");

        return userRepository.save(userMapper.toUserEntity(users));
    }

}
