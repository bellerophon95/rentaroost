package com.kashmira.listingsservice.repository;

import com.kashmira.listingsservice.models.Booking;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface BookingRepository extends MongoRepository<Booking, String> {
}
