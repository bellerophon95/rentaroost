package com.kashmira.listingsservice.config.grpc;

import bookings.BookingServiceGrpc;
import com.kashmira.listingsservice.services.ListingService;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import my_package.PaymentServiceGrpc;
import my_package.PropertyListingServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class GrpcConfig {

    @Autowired
    private LoadBalancerClient loadBalancerClient;

    private ManagedChannel managedChannel;

    public  ManagedChannel getManagedChannel(String serviceName){
        ServiceInstance serviceInstance = loadBalancerClient.choose(serviceName);
        if (serviceInstance == null) {
            throw new IllegalStateException("No instances of "+ serviceName +" found");
        }

        return ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(serviceInstance.getMetadata().get("gRPC_port")))
                .usePlaintext()
                .build();
    }

    @Bean
    public BookingServiceGrpc.BookingServiceBlockingStub bookingServiceBlockingStub() {
        ManagedChannel managedChannel = getManagedChannel("BOOKING-SERVICE");
        return BookingServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub() {
        ManagedChannel managedChannel = getManagedChannel("PAYMENTS-SERVICE");
        return PaymentServiceGrpc.newBlockingStub(managedChannel);
    }

    @Bean
    public PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub() {
        ManagedChannel managedChannel = getManagedChannel("LISTINGS-SERVICE");
        return PropertyListingServiceGrpc.newBlockingStub(managedChannel);
    }


    @PreDestroy
    public void shutdownManagedChannel() {
        if (managedChannel != null) {
            managedChannel.shutdown();
        }
    }
}
