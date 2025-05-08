package com.hepl.budgie.service.impl;

import java.time.Duration;
import java.util.Optional;

//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.hepl.budgie.service.RedisService;

import lombok.extern.slf4j.Slf4j;

//@Service
@Slf4j
public class RedisServiceImpl implements RedisService {

    // private final RedisTemplate<String, Object> redisTemplate;

    // public RedisServiceImpl(RedisTemplate<String, Object> redisTemplate) {
    // this.redisTemplate = redisTemplate;
    // }

    @Override
    public void saveObject(String key, Object value, long mins) {
        log.info("Saving key {}", key);
        // redisTemplate.opsForValue().set(key, value, Duration.ofMinutes(mins));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Optional<T> getObjectUsingKey(String key) {
        log.info("Get object using key {}", key);
        // return Optional.ofNullable((T) redisTemplate.opsForValue().get(key));
        return null;
    }

}
