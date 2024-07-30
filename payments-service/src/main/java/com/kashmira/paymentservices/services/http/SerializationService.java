package com.kashmira.paymentservices.services.http;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
            objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new SerializationException("Failed to serialize object", e);
        }
    }
}