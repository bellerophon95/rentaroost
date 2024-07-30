package com.kashmira.listingsservice.services;//package com.kashmira.graphql_service.services;

import bookings.BookingServiceGrpc;
import bookings.Bookings;
import com.google.gson.Gson;
import com.kashmira.graphql.dgs.types.*;
import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.DgsQuery;
import com.netflix.graphql.dgs.InputArgument;
import my_package.Listings;
import my_package.PaymentServiceGrpc;
import my_package.PropertyListingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.apache.logging.log4j.util.Strings;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@DgsComponent
public class ListingService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final SerializationService serializationService;

    @GrpcClient("listings-client")
    private final PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub;

    public ListingService(
            KafkaTemplate<String, String> kafkaTemplate,
            SerializationService serializationService,
            PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.serializationService = serializationService;
        this.propertyListingServiceBlockingStub = propertyListingServiceBlockingStub;
    }

    @DgsQuery
    public Optional<List<Listing>> fetchListings(@InputArgument ListingFilter listingFilter) {
        return Optional.of(List.of(Listing.newBuilder()
                .id("listing123")
                .hostID("host456")
                .description("A beautiful listing")
                .build()
        ));
    }

    @DgsQuery
    public Listing fetchListing(@InputArgument String listingID) {
        Listings.GetPropertyListingRequest propertyListingRequest = Listings.GetPropertyListingRequest.newBuilder()
                .setPropertyID(listingID)
                .build();

        Listings.GetPropertyListingResponse propertyListing = propertyListingServiceBlockingStub.getPropertyListing(propertyListingRequest);
        Listings.Property property = propertyListing.getProperty();
        boolean doesPropertyExist = !property.getId().equals("-1");

        if (doesPropertyExist) {
            List<Booking> bookings = property.getBookingsList().stream().map(booking -> Booking.newBuilder()
                            .id(booking.getId())
                            .updatedAt(booking.getUpdatedAt())
                            .checkOutDate(booking.getCheckOutDate())
                            .checkInDate(booking.getCheckInDate())
                            .location(Location.newBuilder()
                                    .latitude(booking.getLocation().getLatitude())
                                    .longitude(booking.getLocation().getLongitude())
                                    .build())
                            .createdAt(booking.getCreatedAt())
                            .propertyID(booking.getPropertyID())
                            .totalPrice(booking.getTotalPrice())
                            .build())
                    .toList();

            return Listing.newBuilder()
                    .id(property.getId())
                    .hostID(property.getHostID())
                    .address(property.getAddress())
                    .description(property.getDescription())
                    .bookings(List.of())
                    .createdAt(property.getCreatedAt())
                    .updatedAt(property.getUpdatedAt())
                    .name(property.getName())
                    .bookings(bookings)
                    .build();
        }

        return Listing.newBuilder()
                .id(property.getId())
                .description("Listing not found")
                .id(property.getId())
                .hostID(Strings.EMPTY)
                .address(Strings.EMPTY)
                .bookings(Collections.emptyList())
                .createdAt(property.getCreatedAt())
                .updatedAt(Strings.EMPTY)
                .name(Strings.EMPTY)
                .build();
    }

    @DgsMutation
    public EntityCreationResponse createListing(@InputArgument ListingInput listingInput) { // For now, we're only depending on request validation through GraphQL
        String serializedListingInput = serializationService.serialize(listingInput);

        kafkaTemplate.send("user.listing.creation", serializedListingInput);

        return EntityCreationResponse.newBuilder()
                .message("Listing creation initiated!")
                .build();
    }

    @DgsMutation
    public Listing deleteListingByID(@InputArgument String listingID) {
        return Listing.newBuilder()
                .build();
    }

}
