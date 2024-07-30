package com.kashmira.bookingservice.dtos.stripe;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentDTO {

    StripePaymentMetadataDTO metadata;

}
