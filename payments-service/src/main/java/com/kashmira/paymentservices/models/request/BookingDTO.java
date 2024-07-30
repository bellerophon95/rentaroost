package com.kashmira.paymentservices.models.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDTO {

    private String propertyID;
    private Location location;
    private String guestName;
    private String checkInDate;
    private String checkOutDate;
    private Long totalPrice;
    private String userID;
    String paymentRequestID;
    String amount;
    String initiatedAt;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Location {
        private String latitude;
        private String longitude;
    }
}
