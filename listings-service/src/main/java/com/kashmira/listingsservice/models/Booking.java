package com.kashmira.listingsservice.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Booking {
    String id;
    Location location;
}
