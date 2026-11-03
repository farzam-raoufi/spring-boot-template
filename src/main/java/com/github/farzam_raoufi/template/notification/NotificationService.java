package com.github.farzam_raoufi.template.notification;

import com.github.farzam_raoufi.template.notification.email.EmailRequest;
import com.github.farzam_raoufi.template.notification.sms.SmsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationSenderFactory senderFactory;

    public void sendSms(SmsRequest request) {
        senderFactory.<SmsRequest>getSender(NotificationChannel.SMS).send(request);
    }

    public void sendEmail(EmailRequest request) {
        senderFactory.<EmailRequest>getSender(NotificationChannel.EMAIL).send(request);
    }
}