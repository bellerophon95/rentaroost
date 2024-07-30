package com.kashmira.listingsservice.services;

import bookings.BookingServiceGrpc;
import bookings.Bookings;
import com.google.gson.Gson;
import com.kashmira.graphql.dgs.types.Booking;
import com.kashmira.graphql.dgs.types.BookingInput;
import com.kashmira.graphql.dgs.types.EntityCreationResponse;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@DgsComponent
public class BookingService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SerializationService serializationService;

    @GrpcClient("booking-client")
    private final BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub;
    private final Gson gson;

    public BookingService(
            KafkaTemplate<String, String> kafkaTemplate,
            SerializationService serializationService,
            BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub,
            Gson gson
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.serializationService = serializationService;
        this.bookingServiceBlockingStub = bookingServiceBlockingStub;
        this.gson = gson;
    }

    @DgsQuery
    public Optional<List<Booking>> fetchBookingsByUserID(@InputArgument String userID) {
        Bookings.GetBookingRequest bookingRequestPayload = Bookings.GetBookingRequest
                .newBuilder()
                .setBookingId(userID)
                .build();

        Bookings.Booking fetchedBooking = bookingServiceBlockingStub.getBooking(bookingRequestPayload);

        return Optional.of(List.of(
                Booking.newBuilder()
                        .userID(fetchedBooking.getUserId())
                        .checkInDate(fetchedBooking.getStartDate())
                        .checkOutDate(fetchedBooking.getEndDate())
                        .createdAt(fetchedBooking.getCreatedAt())
                        .updatedAt(fetchedBooking.getUpdatedAt())
                        .build()
        ));
    }

    @DgsMutation
    public EntityCreationResponse createBooking(@InputArgument BookingInput bookingInput) {

        kafkaTemplate.send("user.booking.creation", gson.toJson(bookingInput));

        return EntityCreationResponse.newBuilder()
                .message("Sent request..")
                .build();
    }

    @DgsMutation
    public Booking cancelBookingByID(@InputArgument String bookingID) {
        return Booking.newBuilder()
                .build();
    }
}
