package com.kashmira.listingsservice.services;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.springframework.stereotype.Service;

@Service
public class SerializationService {

    private final ObjectMapper objectMapper;

    public SerializationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String serialize(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object", e);
        }
    }
}