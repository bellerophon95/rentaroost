package com.kashmira.bookingservice.repository;

import com.kashmira.bookingservice.dtos.booking.Booking;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface BookingReactiveRepository extends ReactiveMongoRepository<Booking, String> {
}
