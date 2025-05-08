package com.hepl.budgie.service;

import java.util.Optional;

public interface RedisService {

    void saveObject(String key, Object value, long mins);

    <T> Optional<T> getObjectUsingKey(String key);

}
