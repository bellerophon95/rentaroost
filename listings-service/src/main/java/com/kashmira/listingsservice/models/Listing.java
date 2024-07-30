package com.kashmira.listingsservice.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Listing {

    @Id
    private String listingID;

    private String name;
    private String description;
    private String address;
    private String hostID;
    private double pricePerNight;
    private int bedrooms;
    private int bathrooms;
    private int maxGuests;
    private List<String> amenities;
    private List<String> photoUrls;
    private List<AvailabilityInput> availability;
    private String createdAt;
    private String updatedAt;
    private List<BookingDTO> bookings;
}
