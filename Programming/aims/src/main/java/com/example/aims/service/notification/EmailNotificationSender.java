package com.example.aims.service.notification;

import com.example.aims.dto.request.EmailRequest;
import com.example.aims.dto.request.Recipient;
import com.example.aims.dto.request.SendEmailRequest;
import com.example.aims.dto.request.Sender;
import com.example.aims.exception.AppException;
import com.example.aims.exception.ErrorCode;
import com.example.aims.repository.httpclient.EmailClient;
import feign.FeignException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class EmailNotificationSender implements NotificationSender {

    EmailClient emailClient;

    @Value("${brevo.api-key:}")
    @NonFinal
    private String apiKey;

    @Value("${brevo.sender-name:AIMS}")
    @NonFinal
    private String senderName;

    @Value("${brevo.sender-email:}")
    @NonFinal
    private String senderEmail;

    @Override
    public NotificationChannelType getType() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        SendEmailRequest request = SendEmailRequest.builder()
                .to(Recipient.builder()
                        .name(message.getRecipientName())
                        .email(message.getRecipientAddress())
                        .build())
                .subject(message.getSubject())
                .htmlContent(message.getContent())
                .build();

        if (apiKey == null || apiKey.isBlank() || senderEmail == null || senderEmail.isBlank()) {
            throw new AppException(
                    ErrorCode.CANNOT_SEND_EMAIL.getCode(),
                    "Brevo email configuration is missing"
            );
        }

        Sender sender = Sender.builder()
                .name(senderName)
                .email(senderEmail)
                .build();

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(sender)
                .to(List.of(request.getTo()))
                .subject(request.getSubject())
                .htmlContent(request.getHtmlContent())
                .build();

        try {
            emailClient.sendEmail(apiKey, emailRequest);
        } catch (FeignException e) {
            String responseBody = compact(e.contentUTF8());
            log.error(
                    "Brevo email API rejected request with status {} and body {}",
                    e.status(),
                    responseBody
            );
            throw new AppException(
                    ErrorCode.CANNOT_SEND_EMAIL.getCode(),
                    ErrorCode.CANNOT_SEND_EMAIL.getMessage()
                            + ": Brevo responded with status " + e.status()
                            + " and body " + responseBody
            );
        }
    }

    private String compact(String value) {
        if (value == null || value.isBlank()) {
            return "<empty>";
        }

        String compactValue = value.replaceAll("\\s+", " ").trim();
        return compactValue.length() <= 500 ? compactValue : compactValue.substring(0, 500);
    }
}
