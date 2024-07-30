package org.kashmira.dto.event;


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
public class Event {

    private String timestamp;
    private String userID;
    private String propertyID;
    private String eventType;

}
