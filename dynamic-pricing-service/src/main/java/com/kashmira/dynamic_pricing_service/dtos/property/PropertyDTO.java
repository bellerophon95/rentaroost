package com.kashmira.dynamic_pricing_service.dtos.property;

import com.kashmira.dynamic_pricing_service.dtos.booking.BookingDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
