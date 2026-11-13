package com.hostelcare.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log =
            LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender sender;

    @Value("${spring.mail.username:}")
    private String from;

    public EmailService(JavaMailSender sender) {
        this.sender = sender;
    }

    public void notify(String to, String subject, String body) {

        if (to == null || to.isBlank()
                || from == null || from.isBlank()) {
            log.warn("Email skipped: sender or recipient is missing.");
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();

            message.setFrom(from);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            sender.send(message);

            log.info("Email notification sent to {}", to);

        } catch (Exception ex) {

            log.error(
                "Email notification failed for recipient {}: {}",
                to,
                ex.getMessage()
            );
        }
    }
}
