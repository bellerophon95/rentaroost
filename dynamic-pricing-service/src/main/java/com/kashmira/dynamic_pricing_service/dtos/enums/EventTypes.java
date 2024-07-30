package com.kashmira.dynamic_pricing_service.dtos.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventTypes {
    VIEW("view"),
    BOOKING("booking");

    private final String type;
}
