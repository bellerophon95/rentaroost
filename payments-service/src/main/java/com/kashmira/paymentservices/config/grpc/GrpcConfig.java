package com.kashmira.paymentservices.config.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import my_package.PropertyListingServiceGrpc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;

@Configuration
public class GrpcConfig {
//
//    @Autowired
//    private LoadBalancerClient loadBalancerClient;
//
//    private ManagedChannel managedChannel;
//
//    @Bean
//    public PropertyListingServiceGrpc.PropertyListingServiceBlockingStub propertyListingServiceBlockingStub() {
//        ServiceInstance serviceInstance = loadBalancerClient.choose("LISTINGS-SERVICE");
//        if (serviceInstance == null) {
//            throw new IllegalStateException("No instances of LISTINGS-SERVICE found");
//        }
//
//        managedChannel = ManagedChannelBuilder.forAddress(serviceInstance.getHost(), Integer.parseInt(serviceInstance.getMetadata().get("gRPC_port")))
//                .usePlaintext()
//                .build();
//
//        return PropertyListingServiceGrpc.newBlockingStub(managedChannel);
//    }
//
//    @PreDestroy
//    public void shutdownManagedChannel() {
//        if (managedChannel != null) {
//            managedChannel.shutdown();
//        }
//    }
}
