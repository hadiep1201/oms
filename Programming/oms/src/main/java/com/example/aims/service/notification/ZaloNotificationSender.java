package com.example.aims.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ZaloNotificationSender implements NotificationSender {

    @Override
    public NotificationChannelType getType() {
        return NotificationChannelType.ZALO;
    }

    @Override
    public void send(NotificationMessage message) {
        log.warn("Zalo notification sender is not implemented yet.");
    }
}
