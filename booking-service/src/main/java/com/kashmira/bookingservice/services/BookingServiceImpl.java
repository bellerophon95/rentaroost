package com.kashmira.bookingservice.services;

import bookings.BookingServiceGrpc;
import bookings.Bookings;
import com.kashmira.bookingservice.dtos.booking.Booking;
import com.kashmira.bookingservice.repository.BookingRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import my_package.PaymentServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;


@RequiredArgsConstructor
@GrpcService
public class BookingServiceImpl extends BookingServiceGrpc.BookingServiceImplBase {

    private final BookingRepository bookingRepository;

    @GrpcClient("payment-client")
    public final PaymentServiceGrpc.PaymentServiceBlockingStub paymentServiceBlockingStub;

    @Override
    public void getBooking(Bookings.GetBookingRequest request, StreamObserver<Bookings.Booking> responseObserver) {
        Optional<Booking> retrievedBooking = bookingRepository.findByUserID(request.getBookingId());

        retrievedBooking.ifPresentOrElse(booking -> {
            Bookings.Booking bookingResponse = Bookings.Booking.newBuilder()
                    .setUserId(booking.getUserID())
                    .setStartDate(booking.getCheckInDate())
                    .setEndDate(booking.getCheckOutDate())
                    .setCreatedAt(booking.getCreatedAt())
                    .setUpdatedAt(booking.getUpdatedAt())
                    .setListingId(booking.getPropertyID())
                    .build();

            responseObserver.onNext(bookingResponse);
            responseObserver.onCompleted();
        }, () -> {
            responseObserver.onNext(
                    Bookings.Booking.newBuilder()
                            .build());
        });
    }
}
