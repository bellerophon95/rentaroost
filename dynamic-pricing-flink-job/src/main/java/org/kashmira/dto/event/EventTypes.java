package org.kashmira.dto.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum EventTypes {
    VIEW("view"),
    BOOKING("booking");

    final String eventType;
}
