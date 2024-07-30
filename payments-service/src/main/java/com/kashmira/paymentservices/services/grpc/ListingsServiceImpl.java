package com.kashmira.paymentservices.services.grpc;

import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import my_package.Listings;
import my_package.PropertyListingServiceGrpc;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class ListingsServiceImpl extends PropertyListingServiceGrpc.PropertyListingServiceImplBase {
    @Override
    public void listProperties(Listings.ListPropertiesRequest request, StreamObserver<Listings.ListPropertiesResponse> responseObserver) {
        Listings.ListPropertiesResponse propertyResponse = Listings.ListPropertiesResponse.newBuilder()
                .build();

        responseObserver.onNext(propertyResponse);
        responseObserver.onCompleted();
    }

//    @Override
//    public void createProperty(Listings.CreatePropertyRequest request, StreamObserver<Listings.CreatePropertyResponse> responseObserver) {
//        Listings.CreatePropertyResponse createPropertyResponse = Listings.CreatePropertyResponse.newBuilder()
//                .setProperty(Listings.Property.newBuilder()
//                        .setPropertyID("r21r")
//                        .setAddress("t3t r23t")
//                        .setListedAt(Timestamp.newBuilder()
//                                .setSeconds(3)
//                                .setNanos(2)
//                                .build())
//                        .build())
//                .build();
//        responseObserver.onNext(createPropertyResponse);
//        responseObserver.onCompleted();
//    }
}
