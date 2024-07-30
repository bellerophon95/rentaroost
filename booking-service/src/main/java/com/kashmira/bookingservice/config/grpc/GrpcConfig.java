package com.kashmira.bookingservice.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.RequiredArgsConstructor;
import my_package.PaymentServiceGrpc;
import my_package.PropertyListingServiceGrpc;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
@RequiredArgsConstructor
public class GrpcConfig {

    private final LoadBalancerClient loadBalancerClient;

    @Value("${grpc.service-names.payment}")
    private String paymentsServiceName;

    @Value("${grpc.service-names.listings}")
    private String listingsServiceName;

    @Bean
    public ManagedChannel paymentManagedChannel() {
        return createManagedChannel(paymentsServiceName);
    }

    @Bean
    public PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub(ManagedChannel paymentManagedChannel) {
        return PaymentServiceGrpc.newBlockingStub(paymentManagedChannel);
    }

    @Bean
    public ManagedChannel listingsManagedChannel() {
        return createManagedChannel(listingsServiceName);
    }

    @Bean
    public PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub(ManagedChannel listingsManagedChannel) {
        return PropertyListingServiceGrpc.newBlockingStub(listingsManagedChannel);
    }

    private ManagedChannel createManagedChannel(String serviceName) {
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceName);
        if (serviceInstance == null) {
            throw new IllegalStateException(String.format("No instances of %s found", serviceName));
        }

        return ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(serviceInstance.getMetadata().get("gRPC_port")))
                .usePlaintext()
                .build();
    }

    @PreDestroy
    public void shutdownPaymentManagedChannel() {
        paymentManagedChannel().shutdown();
    }

    @PreDestroy
    public void shutdownListingsManagedChannel() {
        listingsManagedChannel().shutdown();
    }
}
