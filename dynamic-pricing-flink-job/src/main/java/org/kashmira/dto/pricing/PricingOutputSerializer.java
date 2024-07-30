package org.kashmira.dto.pricing;

import org.apache.flink.api.common.serialization.SerializationSchema;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.DeserializationFeature;
import org.apache.flink.shaded.jackson2.com.fasterxml.jackson.databind.ObjectMapper;

public class PricingOutputSerializer implements SerializationSchema<PricingOutput> {

    private static final long serialVersionUID = 1L;
    private transient ObjectMapper objectMapper;

    @Override
    public void open(InitializationContext context) throws Exception {
        this.objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
    }

    @Override
    public byte[] serialize(PricingOutput event) {
        try {
            return objectMapper.writeValueAsBytes(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize Event", e);
        }
    }
}