package com.github.farzam_raoufi.template.notification;


import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationSenderFactory {

    private final Map<NotificationChannel, NotificationSender<? extends NotificationRequest>> senders;

    public NotificationSenderFactory(List<NotificationSender<? extends NotificationRequest>> senders) {
        this.senders = senders.stream()
                .collect(Collectors.toMap(NotificationSender::getChannel, Function.identity()));
    }

    @SuppressWarnings("unchecked")
    public <T extends NotificationRequest> NotificationSender<T> getSender(NotificationChannel channel) {
        NotificationSender<? extends NotificationRequest> sender = senders.get(channel);
        if (sender == null) {
            throw new BusinessException(ErrorMessage.NOTIFICATION_CHANNEL_NOT_SUPPORTED, channel);
        }

        return (NotificationSender<T>) sender;
    }
}
