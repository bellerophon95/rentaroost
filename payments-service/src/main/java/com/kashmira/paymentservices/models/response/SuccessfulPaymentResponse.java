package com.kashmira.paymentservices.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.stripe.model.PaymentIntent;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SuccessfulPaymentResponse {

    @JsonProperty("paymentIntent")
    PaymentIntent paymentIntent;

    @JsonProperty("userID")
    String userID;

    @JsonProperty("propertyID")
    String propertyID;
}
