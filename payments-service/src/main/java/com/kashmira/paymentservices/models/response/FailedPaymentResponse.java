package com.kashmira.paymentservices.models.response;

import com.stripe.model.PaymentIntent;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FailedPaymentResponse {
    String userID;
    String propertyID;
    String errorMessage;
}
