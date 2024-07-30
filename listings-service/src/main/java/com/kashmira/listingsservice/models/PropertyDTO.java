package com.kashmira.listingsservice.models;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyDTO {
    private String id;
    private String name;
    private String description;
    private String address;
    private String hostID;
    private String createdAt;
    private String updatedAt;
    private List<BookingDTO> bookings;
}
