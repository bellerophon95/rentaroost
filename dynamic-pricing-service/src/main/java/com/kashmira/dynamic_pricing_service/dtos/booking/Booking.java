package com.kashmira.dynamic_pricing_service.dtos.booking;

import com.kashmira.dynamic_pricing_service.dtos.location.LocationInput;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
