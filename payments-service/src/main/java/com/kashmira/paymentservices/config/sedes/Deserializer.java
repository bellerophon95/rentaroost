package com.kashmira.paymentservices.config.sedes;

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
public class Deserializer {

    private final ObjectMapper objectMapper;
    public  <T> T deserializeFromJson(String jsonString, Class<T> clazz) {
        try {
            return objectMapper.readValue(jsonString, clazz);
        } catch (JsonProcessingException e) {
            Logger.getLogger("Deserialization").log(Level.SEVERE, "Error deserializing JSON to object", e);
            throw new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR, "Error deserializing JSON to object") {};
        }
    }
}
