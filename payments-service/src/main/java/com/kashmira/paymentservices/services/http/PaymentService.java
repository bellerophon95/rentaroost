package com.kashmira.paymentservices.services.http;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.kashmira.paymentservices.config.sedes.Deserializer;
import com.kashmira.paymentservices.config.sedes.Serializer;
import com.kashmira.paymentservices.models.request.BookingDTO;
import com.kashmira.paymentservices.models.response.FailedPaymentResponse;
import com.kashmira.paymentservices.models.response.SuccessfulPaymentResponse;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.*;
import com.stripe.net.Webhook;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.SetupIntentUpdateParams;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import my_package.Payments;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Scanner;

@Service
public class PaymentService {

    @Value("${stripe.webhooks.endpoint.secret}")
    private String stripeWebhookEndpointSecret;

    private final ObjectMapper objectMapper;
    private static final String DELIMITER = "\\A";
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);
    private Deserializer deserializer;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Gson gson;
    private final SerializationService serializationService;

    public PaymentService(KafkaTemplate<String, String> kafkaTemplate,
                          SerializationService serializationService,
                          Deserializer deserialize,
                          ObjectMapper objectMapper,
                          Gson gson
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.serializationService = serializationService;
        this.deserializer = deserialize;
        this.objectMapper = objectMapper;
        this.gson = gson;
    }

    //    @Transactional
    public ResponseEntity<String> handlePayment(HttpServletRequest request) {
        String payload;
        try (Scanner scanner = new Scanner(request.getInputStream(), StandardCharsets.UTF_8)) {
            payload = scanner.useDelimiter(DELIMITER).next();
        } catch (IOException e) {
            logger.error("Failed to read request payload", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Failed to read request payload");
        }

        String sigHeader = request.getHeader("Stripe-Signature");
        if (sigHeader == null) {
            logger.warn("Missing Stripe-Signature header");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Missing Stripe-Signature header");
        }

        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, stripeWebhookEndpointSecret);
        } catch (SignatureVerificationException e) {
            logger.warn("Invalid signature", e);
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        }

        EventDataObjectDeserializer dataObjectDeserializer = event.getDataObjectDeserializer();
        StripeObject stripeObject = dataObjectDeserializer.getObject().orElse(null);
        if (stripeObject == null) {
            logger.error("Failed to deserialize event data");
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("Failed to deserialize event data");
        }


        switch (event.getType()) {
            case "payment_intent.succeeded":
                PaymentIntent paymentIntent = (PaymentIntent) stripeObject;
                logger.info("Payment for {} succeeded.", paymentIntent.getAmount());

                kafkaTemplate.send("stripe.payment.succeeded", paymentIntent.getId(), serializationService.serialize(paymentIntent));

//                kafkaTemplate.executeInTransaction(ops -> {
//                    try {
//                        kafkaTemplate.send("payment", paymentIntent.getId(), objectMapper.writeValueAsString(paymentIntent));
//                    } catch (JsonProcessingException e) {
//                        throw new RuntimeException(e);
//                    }
//                    return true;
//                });

                break;
            default:
                logger.warn("Unhandled event type: {}", event.getType());
        }

        return ResponseEntity.ok("Webhook handled");
    }


    @KafkaListener(topics = {"user.payment.initiated"}, groupId = "group-id")
    public void initiatePayment(String payload) {

        BookingDTO paymentRequestPayload = gson.fromJson(payload, BookingDTO.class);

        logger.info("Received payment initiation request for userID: {}", paymentRequestPayload.getUserID());

        PaymentIntentCreateParams.AutomaticPaymentMethods automaticPaymentMethodDTO = PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                .setEnabled(true)
                .setAllowRedirects(PaymentIntentCreateParams.AutomaticPaymentMethods.AllowRedirects.NEVER)
                .build();

        Map<String, String> mappedMetadata = Map.of(
                "userID", paymentRequestPayload.getUserID(),
                "propertyID", paymentRequestPayload.getPropertyID(),
                "checkinDate", paymentRequestPayload.getCheckInDate(),
                "checkoutDate", paymentRequestPayload.getCheckOutDate(),
                "totalAmount", String.valueOf(paymentRequestPayload.getTotalPrice())
        );

        PaymentIntentCreateParams paymentIntentCreateParams = PaymentIntentCreateParams.builder()
                .setAmount(paymentRequestPayload.getTotalPrice())
                .setCurrency(SetupIntentUpdateParams.PaymentMethodOptions.AcssDebit.Currency.USD.getValue())
                .setPaymentMethod("pm_card_us")
                .setDescription(paymentRequestPayload.getPropertyID())
                .setAutomaticPaymentMethods(automaticPaymentMethodDTO)
                .putAllMetadata(mappedMetadata)
                .build();

        PaymentIntent paymentIntent;
        try {
            paymentIntent = PaymentIntent.create(paymentIntentCreateParams);
            paymentIntent.confirm();

            SuccessfulPaymentResponse paymentResponse = SuccessfulPaymentResponse.builder()
                    .paymentIntent(paymentIntent)
                    .propertyID(paymentRequestPayload.getPropertyID())
                    .userID(paymentRequestPayload.getUserID())
                    .build();

//            kafkaTemplate.send("user.payment.confirmed", serializationService.serialize(paymentResponse));
            kafkaTemplate.send("user.payment.confirmed", gson.toJson(paymentResponse));


            logger.info("PaymentIntent created successfully with ID: {}", paymentIntent.getId());

        } catch (StripeException e) {
            logger.error("StripeException occurred: ", e);

            FailedPaymentResponse failedPaymentResponse = FailedPaymentResponse.builder()
                    .errorMessage(e.toString())
                    .userID(paymentRequestPayload.getUserID())
                    .propertyID(paymentRequestPayload.getPropertyID())
                    .build();

            kafkaTemplate.send("user.payment.failed", serializationService.serialize(failedPaymentResponse));
        }
    }
}
