package com.kashmira.bookingservice.dtos.location;

import lombok.Builder;
import lombok.Data;

import java.lang.String;

@Data
@Builder
public class Location {
    private String latitude;

    private String longitude;

}
