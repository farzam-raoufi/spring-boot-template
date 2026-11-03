package com.github.farzam_raoufi.template.notification;

public interface NotificationSender<T extends NotificationRequest> {

    NotificationChannel getChannel();

    void send(T request);
}