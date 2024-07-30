package com.kashmira.paymentservices.config.stripe;

import com.stripe.Stripe;
import com.stripe.StripeClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key}")
    private String STRIPE_API_KEY;

    @Value("${stripe.public.api.key}")
    private String STRIPE_PUBLIC_API_KEY;

    @Bean
    @Qualifier("stripeAPIKey")
    public String apiKey(){
        return STRIPE_API_KEY;
    }

    @Bean
    public StripeClient stripeClient(){
        Stripe.apiKey = STRIPE_API_KEY;

        return StripeClient
                .builder()
                .setApiKey(STRIPE_API_KEY)
                .build();
    }

}
