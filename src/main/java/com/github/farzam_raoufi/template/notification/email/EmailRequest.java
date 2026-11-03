package com.github.farzam_raoufi.template.notification.email;

import com.github.farzam_raoufi.template.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailRequest implements NotificationRequest {

    private String to;
    private String subject;
    private String body;
}