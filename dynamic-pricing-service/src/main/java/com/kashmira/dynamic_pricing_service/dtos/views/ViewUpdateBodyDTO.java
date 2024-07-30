package com.kashmira.dynamic_pricing_service.dtos.views;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
public class ViewUpdateBodyDTO {
    private String propertyId;
    private String userId;
    private LocalDateTime timestamp;
}
