package com.kashmira.dynamic_pricing_service.dtos.booking;

import com.kashmira.dynamic_pricing_service.dtos.location.LocationDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
