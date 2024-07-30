package com.kashmira.paymentservices.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StripePaymentDTO {

    Metadata metadata;

}
