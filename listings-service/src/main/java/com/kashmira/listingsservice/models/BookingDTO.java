package com.kashmira.listingsservice.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingDTO {
    private String id;
    private String propertyID;
    private String userID;
    private String checkInDate;
    private String checkOutDate;
    private double totalPrice;
    private LocationDTO location;
    private String createdAt;
    private String updatedAt;
}
