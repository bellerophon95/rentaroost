package com.kashmira.bookingservice.dtos.booking;

import com.kashmira.bookingservice.dtos.location.LocationInput;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking {

    @Id
    private String id;

    private String propertyID;
    private LocationInput location;
    private String checkInDate;
    private String checkOutDate;
    private double totalPrice;
    private String userID;
    private String createdAt;
    private String updatedAt;

}
