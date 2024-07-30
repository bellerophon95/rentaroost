package com.kashmira.paymentservices.config.sedes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.stripe.model.StripeRawJsonObject;
import com.stripe.model.StripeRawJsonObjectDeserializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Serializer {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public Gson GSON() {
        GsonBuilder gsonBuilder = new GsonBuilder();

        return gsonBuilder
                .registerTypeAdapter(StripeRawJsonObject.class, new StripeRawJsonObjectDeserializer())
                .create();

    }

}