package com.kashmira.listingsservice.services;

import com.google.gson.Gson;
import com.google.protobuf.Timestamp;
import com.kashmira.listingsservice.config.sedes.Deserializer;
import com.kashmira.listingsservice.models.Booking;
import com.kashmira.listingsservice.models.Listing;
import com.kashmira.listingsservice.repository.BookingRepository;
import com.kashmira.listingsservice.repository.ListingRepository;
import com.kashmira.listingsservice.repository.ReactiveListingRepository;
import lombok.RequiredArgsConstructor;
import my_package.Payments;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Calendar;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ListingsService {
    private final ReactiveListingRepository reactiveListingRepository;

//    @GrpcClient("payment-client")
//    private final PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub;

    private final Deserializer deserializer;

    @KafkaListener(topics = "user.listing.creation", groupId = "group-id")
    public void listingCreationListener(String listingCreationPayloadDTO) {
        Listing listing = deserializer.deserializeFromJson(listingCreationPayloadDTO, Listing.class);

        listing.setCreatedAt(String.valueOf(Calendar.getInstance().getTimeInMillis()));
        listing.setUpdatedAt(String.valueOf(Calendar.getInstance().getTimeInMillis()));

        Mono.just(listing)
                .flatMap(reactiveListingRepository::save)
                .subscribe(savedListing -> {
                    Logger.getLogger("Booking repository saved").log(Level.FINE, "Booking saved for userId" + savedListing.getListingID());
                }, error -> {
                    Logger.getLogger("Booking repository saved").log(Level.FINE, "Booking saved for userId" + error.getMessage());
                });

    }

}
