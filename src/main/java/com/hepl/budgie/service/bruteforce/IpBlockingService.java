package com.hepl.budgie.service.bruteforce;

public interface IpBlockingService {

    boolean isIpBlocked(String ip);

    boolean recordRequest(String ip);
}
