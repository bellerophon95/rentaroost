package com.kashmira.bookingservice.dtos.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentMetadataDTO {
    private String checkoutDate;
    private String checkinDate;
    private String userID;
    private String totalAmount;
    private String propertyID;
}
