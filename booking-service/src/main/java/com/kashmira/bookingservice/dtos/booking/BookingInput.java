package com.kashmira.bookingservice.dtos.booking;

import com.kashmira.bookingservice.dtos.location.LocationInput;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingInput {
    private String propertyID;
    private LocationInput location;
    private String checkInDate;
    private String checkOutDate;
    private double totalPrice;
    private String userID;
}