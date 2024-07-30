package com.kashmira.listingsservice.models;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationInput {
    private String latitude;
    private String longitude;
}
