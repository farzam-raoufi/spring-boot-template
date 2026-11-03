package com.github.farzam_raoufi.template.notification.sms;

import com.github.farzam_raoufi.template.notification.NotificationRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsRequest implements NotificationRequest {

    private String phoneNumber;
    private String patternCode;
    private Map<String, String> variables;
}