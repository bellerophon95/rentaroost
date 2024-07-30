package com.kashmira.bookingservice.services;

import com.google.protobuf.Timestamp;
import com.kashmira.bookingservice.config.sedes.Deserializer;
import com.kashmira.bookingservice.config.sedes.Serializer;
import com.kashmira.bookingservice.dtos.booking.Booking;
import com.kashmira.bookingservice.dtos.booking.BookingDAO;
import com.kashmira.bookingservice.dtos.booking.BookingInput;
import com.kashmira.bookingservice.dtos.booking.BookingPaymentRequestDTO;
import com.kashmira.bookingservice.dtos.stripe.StripePaymentDTO;
import com.kashmira.bookingservice.dtos.stripe.StripePaymentMetadataDTO;
import com.kashmira.bookingservice.repository.BookingReactiveRepository;
import lombok.RequiredArgsConstructor;
import my_package.Listings;
import my_package.Payments;
import my_package.PropertyListingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Calendar;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger logger = LoggerFactory.getLogger(BookingService.class);

    private final RedisTemplate<Object, Object> redisTemplate;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final BookingReactiveRepository bookingReactiveRepository;
    private final Serializer serializer;
    private final Deserializer deserializer;

    @GrpcClient("listings-client")
    private final PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub;


    @KafkaListener(topics = "user.booking.creation", groupId = "group-id")
    public void bookingCreationListener(String bookingPayloadDTO) {
        try {
            BookingInput bookingRequestDTO = deserializer.deserializeFromJson(bookingPayloadDTO, BookingInput.class);

            Object dynamicPrice = getDynamicPrice(bookingRequestDTO);

            Payments.PaymentRequestPayload paymentRequestPayload = Payments.PaymentRequestPayload.newBuilder()
                    .setPropertyID(bookingRequestDTO.getPropertyID())
                    .setAmount((long)(Double.parseDouble(dynamicPrice.toString()))) // Fetch prices from dynamically-priced redis or listing collection if not found
                    .setInitiatedAt(Timestamp.newBuilder()
                            .setSeconds(Calendar.getInstance().getTimeInMillis())
                            .build())
                    .setUserID(bookingRequestDTO.getUserID())
                    .build();

            BookingPaymentRequestDTO bookingPaymentRequestDTO = BookingPaymentRequestDTO.builder()
                    .propertyID(bookingRequestDTO.getPropertyID())
                    .userID(bookingRequestDTO.getUserID())
                    .amount(dynamicPrice.toString())
                    .initiatedAt(String.valueOf(Calendar.getInstance().getTimeInMillis()))
                    .build();

            kafkaTemplate.send("user.payment.initiated", serializer.serializeToJson(bookingPaymentRequestDTO));
            logger.info("Booking creation processed and payment initiated for propertyID: {}", bookingPaymentRequestDTO.getPropertyID());
        } catch (Exception e) {
            logger.error("Failed to process booking creation", e);
        }
    }

    private Object getDynamicPrice(BookingInput bookingRequestDTO) {
        Listings.GetPropertyListingResponse getPropertyListingResponse = fetchPropertyListingByID(bookingRequestDTO);

        Object dynamicPrice;
        if(Boolean.TRUE.equals(redisTemplate.opsForHash().hasKey("property:" + bookingRequestDTO.getPropertyID(), "currentPrice"))){
            return redisTemplate.opsForHash().get("property:" + bookingRequestDTO.getPropertyID(), "currentPrice");
        }
        return getPropertyListingResponse.getProperty();
    }

    private Listings.GetPropertyListingResponse fetchPropertyListingByID(BookingInput bookingRequestDTO) {
        Listings.GetPropertyListingRequest propertyListingRequest = Listings.GetPropertyListingRequest.newBuilder()
                .setPropertyID(bookingRequestDTO.getPropertyID())
                .build();

        return propertyListingServiceBlockingStub.getPropertyListing(propertyListingRequest);
    }

    @KafkaListener(topics = "stripe.payment.succeeded", groupId = "group-id")
    public void stripePaymentSuccessListener(String bookingPayloadDTO) {
        try {
            StripePaymentDTO stripePaymentDTO = deserializer.deserializeFromJson(bookingPayloadDTO, StripePaymentDTO.class);
            StripePaymentMetadataDTO metadataDTO = stripePaymentDTO.getMetadata();

            Booking booking = Booking.builder()
                    .userID(metadataDTO.getUserID())
                    .checkOutDate(metadataDTO.getCheckoutDate())
                    .checkInDate(metadataDTO.getCheckinDate())
                    .propertyID(metadataDTO.getPropertyID())
                    .createdAt(String.valueOf(Calendar.getInstance().getTimeInMillis()))
                    .updatedAt(String.valueOf(Calendar.getInstance().getTimeInMillis()))
                    .totalPrice(Double.parseDouble(metadataDTO.getTotalAmount()))
                    .build();

            kafkaTemplate.send("user.booking.confirmed", serializer.serializeToJson(booking));

            Mono.just(booking)
                    .flatMap(bookingReactiveRepository::save)
                    .subscribe(savedBooking -> {
                        logger.info("Booking successfully saved for userId: {}", metadataDTO.getUserID());
                    }, error -> {
                        logger.error("Failed to save booking for userId: {}", metadataDTO.getUserID(), error);
                    });

        } catch (Exception e) {
            kafkaTemplate.send("user.booking.failed", bookingPayloadDTO);
            logger.error("Failed to process stripe payment success", e);
        }
    }
}
