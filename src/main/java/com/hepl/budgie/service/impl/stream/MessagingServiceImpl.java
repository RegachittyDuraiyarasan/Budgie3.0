package com.hepl.budgie.service.impl.stream;

import java.util.concurrent.CompletableFuture;

import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.stereotype.Service;

import com.hepl.budgie.dto.notification.Notification;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MessagingServiceImpl {

    private final StreamBridge streamBridge;

    public MessagingServiceImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    public void sendMessage(Notification notification) {
        log.info("Sending notification ...");
        CompletableFuture<Void> notify = CompletableFuture
                .runAsync(() -> streamBridge.send("notification-topic", notification));
        notify.thenAccept(result -> log.info("Sending  ..."));
    }

}
