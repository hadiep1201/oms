package com.example.aims.service.notification;

public interface NotificationSender {
    NotificationChannelType getType();
    void send(NotificationMessage message);
}
