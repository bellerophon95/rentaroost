package com.kashmira.listingsservice.models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Location {
    private String latitude;
    private String longitude;
}
