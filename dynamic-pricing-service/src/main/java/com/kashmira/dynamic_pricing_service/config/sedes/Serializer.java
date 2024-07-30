package com.kashmira.dynamic_pricing_service.config.sedes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
@RequiredArgsConstructor
public class Serializer {

    private final ObjectMapper objectMapper;

    public String serializeToJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            Logger.getLogger("Serialization").log(Level.SEVERE, "Error serializing object to JSON", e);
            throw new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing object to JSON") {
            };
        }
    }
}