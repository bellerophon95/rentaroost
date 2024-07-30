package com.kashmira.gateway.config.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // Example route for GraphQL requests
                .route("graphql-route", r -> r.path("/graphql/**")
                        .uri("http://graphql-server-host:port/graphql"))
                // Add more routes as needed for other APIs
                .build();
    }
}
