package com.kashmira.bookingservice.dtos.booking;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class BookingPaymentRequestDTO {
    String userID;
    String paymentRequestID;
    String propertyID;
    String amount;
    String initiatedAt;
}
