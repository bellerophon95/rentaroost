package com.kashmira.dynamic_pricing_service.dtos.location;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LocationInput {
    private String latitude;
    private String longitude;
}
