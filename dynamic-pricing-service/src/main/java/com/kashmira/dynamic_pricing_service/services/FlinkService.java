package com.kashmira.dynamic_pricing_service.services;

import com.kashmira.dynamic_pricing_service.config.sedes.Deserializer;
import com.kashmira.dynamic_pricing_service.config.sedes.Serializer;
import com.kashmira.dynamic_pricing_service.dtos.booking.BookingDTO;
import com.kashmira.dynamic_pricing_service.dtos.enums.EventTypes;
import com.kashmira.dynamic_pricing_service.dtos.flink.FlinkViewInputEvent;
import com.kashmira.dynamic_pricing_service.dtos.flink.FlinkViewOutputEvent;
import com.kashmira.dynamic_pricing_service.dtos.property.PropertyDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Service for handling Kafka events and processing them with Flink.
 * <p>
 * This service listens to Kafka topics related to user interactions with listings and bookings,
 * and forwards the events to a Kafka topic for dynamic pricing updates.
 * <p>
 * Details:
 * - `user.listings.view`: Listens for view events on listings. Converts the event into a Flink input event
 * and sends it to the `dynamicpricing.update` topic.
 * - `user.booking.confirmed`: Listens for booking confirmation events. Converts the event into a Flink
 * input event and sends it to the `dynamicpricing.update` topic.
 * - `dynamicpricing.output`: Receives updated pricing information from Flink.
 * (Currently, this method is commented out but is intended to handle responses by updating Redis.)
 * <p>
 * Note:
 * - The `TEST_USER_ID` is used for all events as a placeholder. OAuth2 support and user roles will be
 * implemented in the future to enable a more sophisticated approach.
 * - To test the Flink job, you can use the following command:
 * `kafka-console-producer --broker-list localhost:9092 --topic prod2 < ./event.json`
 * Ensure that Zookeeper, Kafka, and the Flink cluster are deployed and the job is submitted.
 */

@Service
@RequiredArgsConstructor
public class FlinkService {

    private final Deserializer deserializer;
    private final Serializer serializer;
    private final RedisTemplate<String, String> redisTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);
    private static final String TEST_USER_ID = "U0987654321";
    private static final Logger LOGGER = Logger.getLogger(FlinkService.class.getName());

    @KafkaListener(topics = {"user.listings.view"}, groupId = "group-id")
    public void toListingFlink(String viewUpdateDTO) {
        try {
            PropertyDTO propertyDTO = deserializer.deserializeFromJson(viewUpdateDTO, PropertyDTO.class);
            FlinkViewInputEvent inputViewEvent = FlinkViewInputEvent.builder()
                    .eventType(EventTypes.VIEW.getType())
                    .timestamp(ISO_FORMATTER.format(Instant.now()))
                    .propertyID(propertyDTO.getId())
                    .userID(TEST_USER_ID)
                    .build();

            kafkaTemplate.send("dynamicpricing.update", serializer.serializeToJson(inputViewEvent));

        } catch (Exception e) {
            LOGGER.severe("Error processing listing view event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = {"user.booking.confirmed"}, groupId = "group-id")
    public void toBookingFlink(String bookingConfirmationDTO) {
        try {
            BookingDTO bookingDTO = deserializer.deserializeFromJson(bookingConfirmationDTO, BookingDTO.class);
            FlinkViewInputEvent inputViewEvent = FlinkViewInputEvent.builder()
                    .eventType(EventTypes.BOOKING.getType())
                    .timestamp(ISO_FORMATTER.format(Instant.now()))
                    .propertyID(bookingDTO.getPropertyID())
                    .userID(TEST_USER_ID)
                    .build();

            kafkaTemplate.send("dynamicpricing.update", serializer.serializeToJson(inputViewEvent));

        } catch (Exception e) {
            LOGGER.severe("Error processing booking confirmation event: " + e.getMessage());
        }
    }

    @KafkaListener(topics = {"dynamicpricing.output"}, groupId = "group-id")
    public void fromFlink(String viewUpdateBodyDTO) {
        FlinkViewOutputEvent flinkViewOutputEvent = deserializer.deserializeFromJson(viewUpdateBodyDTO, FlinkViewOutputEvent.class);

        /**
         * this will ensure that events that fall out of the current window are removed from redis. A future improvement is to use redis keyspace notifs
         * to trigger a price drop event to our push notification flow.
         */
        redisTemplate.expire(flinkViewOutputEvent.getPropertyID(), Duration.ofMinutes(30));

        redisTemplate.opsForHash().put("property:" + flinkViewOutputEvent.getPropertyID(), "currentPrice", String.valueOf(flinkViewOutputEvent.getAdjustedPrice()));

    }
}
