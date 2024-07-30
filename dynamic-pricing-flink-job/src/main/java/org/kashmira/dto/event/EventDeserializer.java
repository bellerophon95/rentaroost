package org.kashmira.dto.event;

import org.apache.flink.api.common.serialization.AbstractDeserializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Deserialization schema for deserializing Event objects from byte arrays.
 */
public class EventDeserializer extends AbstractDeserializationSchema<Event> {

    private static final long serialVersionUID = 1L;

    private transient ObjectMapper objectMapper;

    /**
     * Initialize the ObjectMapper. This method is called once when the schema is opened.
     */
    @Override
    public void open(InitializationContext context) {
        objectMapper = new ObjectMapper();
    }

    /**
     * Deserialize byte array into Event object.
     *
     * @param message The byte array containing the serialized Event object.
     * @return The deserialized Event object.
     * @throws IOException If deserialization fails.
     */
    @Override
    public Event deserialize(byte[] message) throws IOException {
        // Convert byte array to string and strip the extra quotes
        String jsonString = new String(message, StandardCharsets.UTF_8);
        jsonString = jsonString.replaceAll("^\"|\"$", "");

        // Deserialize the cleaned JSON string into Event object
        return objectMapper.readValue(jsonString, Event.class);
    }
}
