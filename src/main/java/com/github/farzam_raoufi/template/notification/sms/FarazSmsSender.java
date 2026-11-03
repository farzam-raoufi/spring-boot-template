package com.github.farzam_raoufi.template.notification.sms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.farzam_raoufi.template.exception.BusinessException;
import com.github.farzam_raoufi.template.exception.ErrorMessage;
import com.github.farzam_raoufi.template.notification.NotificationChannel;
import com.github.farzam_raoufi.template.notification.NotificationSender;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Component
public class FarazSmsSender implements NotificationSender<SmsRequest> {

    private static final String PATTERN_SEND_URL = "https://api2.ippanel.com/api/v1/sms/pattern/normal/send";

    private final RestClient restClient;
    private final String apiKey;
    private final String senderLine;

    public FarazSmsSender(RestClient.Builder restClientBuilder,
                          @Value("${app.notification.sms.faraz.api-key}") String apiKey,
                          @Value("${app.notification.sms.faraz.sender-line}") String senderLine) {
        this.restClient = restClientBuilder.build();
        this.apiKey = apiKey;
        this.senderLine = senderLine;
    }

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(SmsRequest request) {
        PatternSendRequest body = new PatternSendRequest(
                request.getPatternCode(),
                senderLine,
                request.getPhoneNumber(),
                request.getVariables()
        );

        try {
            restClient.post()
                    .uri(PATTERN_SEND_URL)
                    .header("apikey", apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            log.error("Failed to send SMS via Faraz. phone={}, pattern={}", request.getPhoneNumber(), request.getPatternCode(), e);
            throw new BusinessException(ErrorMessage.NOTIFICATION_SEND_FAILED, NotificationChannel.SMS);
        }
    }

    private record PatternSendRequest(
            @JsonProperty("code") String code,
            @JsonProperty("sender") String sender,
            @JsonProperty("recipient") String recipient,
            @JsonProperty("variable") Map<String, String> variable
    ) {
    }
}