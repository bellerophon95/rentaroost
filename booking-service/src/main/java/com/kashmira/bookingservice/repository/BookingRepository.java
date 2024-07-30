package com.kashmira.bookingservice.repository;

import com.kashmira.bookingservice.dtos.booking.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BookingRepository extends MongoRepository<Booking, String> {
    Optional<Booking> findByUserID(String userID);
}
