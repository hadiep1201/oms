package com.example.aims.service.notification;

import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class NotificationSenderResolver {

    private final Map<NotificationChannelType, NotificationSender> senders;

    public NotificationSenderResolver(List<NotificationSender> senderList) {
        this.senders = new EnumMap<>(NotificationChannelType.class);

        for (NotificationSender sender : senderList) {
            senders.put(sender.getType(), sender);
        }
    }

    public NotificationSender resolve(NotificationChannelType type) {
        NotificationSender sender = senders.get(type);

        if (sender == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + type);
        }

        return sender;
    }
}
