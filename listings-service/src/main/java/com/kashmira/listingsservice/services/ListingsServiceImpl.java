package com.kashmira.listingsservice.services;

import com.google.gson.Gson;
import com.kashmira.listingsservice.config.gson.GsonConfig;
import com.kashmira.listingsservice.models.BookingDTO;
import com.kashmira.listingsservice.models.Listing;
import com.kashmira.listingsservice.models.PropertyDTO;
import com.kashmira.listingsservice.repository.ListingRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import my_package.Listings;
import my_package.PropertyListingServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@GrpcService
@RequiredArgsConstructor
public class ListingsServiceImpl extends PropertyListingServiceGrpc.PropertyListingServiceImplBase {

    private final ListingRepository listingRepository;

    private final KafkaTemplate<String, String> kafkaTemplate;

    private final Gson GSON;

    @Override
    public void getPropertyListing(Listings.GetPropertyListingRequest request, StreamObserver<Listings.GetPropertyListingResponse> responseObserver) {
        Optional<Listing> retrievedProperty = listingRepository.findByListingID(request.getPropertyID());

        retrievedProperty.ifPresentOrElse(property -> {
                    List<Listings.Booking> bookingsList = property.getBookings().stream().map(bookingDTO -> Listings.Booking.newBuilder()
                                    .setCheckInDate(bookingDTO.getCheckInDate())
                                    .setCheckOutDate(bookingDTO.getCheckOutDate())
                                    .setUserID(bookingDTO.getUserID())
                                    .setUpdatedAt(bookingDTO.getUpdatedAt())
                                    .setLocation(Listings.Location.newBuilder()
                                            .setLatitude(bookingDTO.getLocation().getLatitude())
                                            .setLongitude(bookingDTO.getLocation().getLongitude())
                                            .build())
                                    .setPropertyID(bookingDTO.getPropertyID())
                                    .setTotalPrice(bookingDTO.getTotalPrice())
                                    .setCreatedAt(bookingDTO.getCreatedAt())
                                    .setTotalPrice(bookingDTO.getTotalPrice())
                                    .build())
                            .toList();

                    Listings.Property propertyToSend = Listings.Property.newBuilder()
                            .setId(property.getListingID())
                            .setDescription(property.getDescription())
                            .setAddress(property.getAddress())
                            .setHostID(property.getHostID())
                            .setName(property.getName())
                            .setCreatedAt(property.getCreatedAt())
                            .setUpdatedAt(property.getUpdatedAt())
                            .addAllBookings(bookingsList)
                            .build();

                    Listings.GetPropertyListingResponse propertyListingResponse = Listings.GetPropertyListingResponse.newBuilder()
                            .setProperty(propertyToSend)
                            .build();

                    PropertyDTO propertyDTOEvent = PropertyDTO.builder()
                            .id(property.getListingID())
                            .description(property.getDescription())
                            .address(property.getAddress())
                            .build();

                    String stringifiedPropertyEvent = GSON.toJson(propertyDTOEvent);

                    kafkaTemplate.send("user.listings.view", stringifiedPropertyEvent);

                    responseObserver.onNext(propertyListingResponse);

                    responseObserver.onCompleted();
                },
                () -> {
                    Listings.GetPropertyListingResponse emptyPropertyListingResponse = Listings.GetPropertyListingResponse.newBuilder()
                            .setProperty(Listings.Property.newBuilder()
                                    .setId("-1")
                                    .build())
                            .build();
                    responseObserver.onNext(emptyPropertyListingResponse);
                    responseObserver.onCompleted();
                }
        );

    }
}
