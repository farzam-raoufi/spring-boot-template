package com.github.farzam_raoufi.template.notification.email;

import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.notification.NotificationChannel;
import com.github.farzam_raoufi.template.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender<EmailRequest> {

    private final JavaMailSender mailSender;
    private final String fromAddress;

    public EmailNotificationSender(JavaMailSender mailSender,
                                   @Value("${app.notification.email.from}") String fromAddress) {
        this.mailSender = mailSender;
        this.fromAddress = fromAddress;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(EmailRequest request) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(request.getTo());
        message.setSubject(request.getSubject());
        message.setText(request.getBody());

        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Failed to send email. to={}, subject={}", request.getTo(), request.getSubject(), e);
            throw new BusinessException(ErrorMessage.NOTIFICATION_SEND_FAILED, NotificationChannel.EMAIL);
        }
    }
}