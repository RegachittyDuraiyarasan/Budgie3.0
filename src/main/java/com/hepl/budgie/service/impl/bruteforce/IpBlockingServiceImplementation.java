package com.hepl.budgie.service.impl.bruteforce;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.hepl.budgie.service.bruteforce.IpBlockingService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class IpBlockingServiceImplementation implements IpBlockingService {

    // Cache to store request counts per IP with 1-minute expiration
    private final Cache<String, Integer> requestCountCache;

    // Cache to store blocked IPs with 1-minute block duration
    private Cache<String, Long> blockedIpsCache;

    private static final int MAX_REQUESTS_PER_MINUTE = 50;
    private static final int BLOCK_DURATION_MINUTES = 1;

    public IpBlockingServiceImplementation() {
        // Cache for tracking the number of requests per IP (expires after 1 minute)
        requestCountCache = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        // Cache for blocked IPs (expires after 1 minute)
        blockedIpsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(BLOCK_DURATION_MINUTES, TimeUnit.MINUTES)
                .build();
    }

    @Override
    public boolean isIpBlocked(String ip) {
        Long blockedUntil = blockedIpsCache.getIfPresent(ip);
        return blockedUntil != null && blockedUntil > System.currentTimeMillis();
    }

    @Override
    public boolean recordRequest(String ip) {
        if (isIpBlocked(ip)) {
            return true; // IP is blocked
        }

        // Get the current request count for the IP
        Integer requests = requestCountCache.getIfPresent(ip);
        if (requests == null) {
            requests = 0;
        }
        requests++;

        requestCountCache.put(ip, requests);

        if (requests > MAX_REQUESTS_PER_MINUTE) {
            log.info("Size {}", requests);
            blockIp(ip);
            return true; // Ip is blocked
        }

        return false;
    }

    // Block the IP address for a certain duration
    private void blockIp(String ip) {
        long blockUntil = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(BLOCK_DURATION_MINUTES);
        blockedIpsCache.put(ip, blockUntil);
    }
}
