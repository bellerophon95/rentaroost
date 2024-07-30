package com.kashmira.bookingservice.config.sedes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
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
            throw new HttpStatusCodeException(HttpStatus.INTERNAL_SERVER_ERROR, "Error serializing object to JSON") {};
        }
    }

    @Bean
    public Gson GSON() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        return gsonBuilder
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create();

    }

}

