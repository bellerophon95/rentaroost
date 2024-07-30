package com.kashmira.paymentservices.models.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Metadata {
    String checkoutDate;
    String checkinDate;
    String userID;
    String propertyID;
}