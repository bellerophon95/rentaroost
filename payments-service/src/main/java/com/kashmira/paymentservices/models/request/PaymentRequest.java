package com.kashmira.paymentservices.models.request;

import com.google.type.DateTime;
import com.kashmira.paymentservices.models.eums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentRequest {
    PaymentMethod paymentMethod;
    DateTime initiatedDate;

}
