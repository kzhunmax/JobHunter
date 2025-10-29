package com.github.kzhunmax.jobsearch.event.consumer;

import com.github.kzhunmax.jobsearch.shared.event.EventType;
import com.github.kzhunmax.jobsearch.shared.event.PasswordResetEvent;
import com.github.kzhunmax.jobsearch.shared.event.UserEvent;
import com.github.kzhunmax.jobsearch.shared.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationConsumer {

    private final EmailService emailService;

    @KafkaListener(
            containerFactory = "kafkaListenerContainerFactory",
            topics = "user-events",
            groupId = "user-notify-group"
    )
    public void onUserEvent(UserEvent event) {
        if (EventType.REGISTERED.equals(event.eventType())) {
            log.info("Sending verification email to {}", event.email());
            emailService.sendVerificationEmail(event.email());
        }
    }

    @KafkaListener(
            containerFactory = "kafkaListenerContainerFactory",
            topics = "password-reset-events",
            groupId = "password-reset-group"
    )
    public void onPasswordResetEvent(PasswordResetEvent event) {
        log.info("Sending password reset email to {}", event.email());
        emailService.sendPasswordResetEmail(event.email(), event.token());
    }
}
