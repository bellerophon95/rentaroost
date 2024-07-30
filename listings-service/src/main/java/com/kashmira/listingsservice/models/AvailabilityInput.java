package com.kashmira.listingsservice.models;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AvailabilityInput {
    private String startDate;
    private String endDate;
}
