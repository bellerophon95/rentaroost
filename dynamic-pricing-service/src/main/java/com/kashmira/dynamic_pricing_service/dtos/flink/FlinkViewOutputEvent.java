package com.kashmira.dynamic_pricing_service.dtos.flink;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class FlinkViewOutputEvent {
    private String propertyID;
    private String eventType;
    private double adjustedPrice;
}
