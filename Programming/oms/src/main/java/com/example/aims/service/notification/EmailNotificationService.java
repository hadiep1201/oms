package com.example.aims.service.notification;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.aims.exception.AppException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailNotificationService {

    NotificationSenderResolver notificationSenderResolver;

    public void sendEmailUpdateNotification(String oldEmail, String newEmail, String userName) {
        NotificationMessage message = NotificationMessage.builder()
                .recipientName(userName)
                .recipientAddress(newEmail)
                .subject("AIMS - Your account email has been updated")
                .content(buildEmailContent(userName, oldEmail, newEmail))
                .build();

        try {
            notificationSenderResolver.resolve(NotificationChannelType.EMAIL).send(message);
            log.info("Sent email update notification for user {} to {}", userName, newEmail);
        } catch (AppException ex) {
            log.error("Failed to send email update notification for user {}: {}", userName, ex.getMessage());
        }
    }

    private String buildEmailContent(String userName, String oldEmail, String newEmail) {
        return """
                <html>
                    <body>
                        <h2>AIMS Account Security Alert</h2>
                        <p>Hello %s,</p>
                        <p>Your email address has been updated by an Administrator.</p>
                        <p><strong>Old Email:</strong> %s</p>
                        <p><strong>New Email:</strong> %s</p>
                        <p>If you did not request this change, please contact support immediately.</p>
                    </body>
                </html>
                """.formatted(userName, oldEmail, newEmail);
    }
}
