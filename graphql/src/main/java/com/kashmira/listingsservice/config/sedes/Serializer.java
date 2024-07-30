package com.kashmira.listingsservice.config.sedes;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Serializer {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

}