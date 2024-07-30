package com.kashmira.pushnotification.controller;

import com.google.firebase.messaging.*;
import com.kashmira.pushnotification.model.PushNotificationRequest;
import com.kashmira.pushnotification.model.PushNotificationResponse;
import com.kashmira.pushnotification.service.PushNotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class PushNotificationController {

    private final PushNotificationService pushNotificationService;

    private final String NOTIFICATION_MESSAGE = "Notification has been sent.";

    public PushNotificationController(PushNotificationService pushNotificationService) {
        this.pushNotificationService = pushNotificationService;
    }

    @GetMapping("/samplereq")
    @KafkaListener(topics = "payment", groupId = "group-id")
    public ResponseEntity<PushNotificationResponse> sampleReq(String message) {
        // Assuming PushNotificationRequest includes a token field

        PushNotificationRequest pushNotificationRequest = PushNotificationRequest.builder()
                .topic("news")
                .message(message)
                .title("Title 1")
                .notification(new Notification("Payment update.", message))
                .build();

        Map<String, String> data = Map.of(
                "name", "Kashmira",
                "title", "Payment Update"
        );

        pushNotificationService.sendPushNotification(pushNotificationRequest);

        return ResponseEntity.ok(new PushNotificationResponse(HttpStatus.OK.value(), NOTIFICATION_MESSAGE));
    }

    @PostMapping("/saveToken")
    public ResponseEntity<String> saveToken(@RequestBody Map<String, String> requestBody) {
        String token = requestBody.get("token");
        pushNotificationService.sendPushNotificationToToken(PushNotificationRequest.builder()
                .token(token)
                .build());
        return ResponseEntity.ok("Token saved successfully");
    }

    @PostMapping("/subscribeToTopic")
    public String subscribeToTopic(@RequestBody Map<String, String> payload) {
        String token = payload.get("token");
        String topic = payload.get("topic");

        try {
            TopicManagementResponse response = FirebaseMessaging.getInstance().subscribeToTopic(
                    java.util.Collections.singletonList(token), topic);
            return "Successfully subscribed to topic: " + response.getSuccessCount();
        } catch (Exception e) {
            return "Error subscribing to topic: " + e.getMessage();
        }
    }
}
