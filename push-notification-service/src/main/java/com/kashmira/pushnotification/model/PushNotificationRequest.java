package com.kashmira.pushnotification.model;

import com.google.firebase.messaging.Notification;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class PushNotificationRequest {

    private String title;
    private String message;
    private String topic;
    private String token;
    private Notification notification;


}
