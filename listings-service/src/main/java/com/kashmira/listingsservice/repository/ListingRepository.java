package com.kashmira.listingsservice.repository;

import com.kashmira.listingsservice.models.Listing;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ListingRepository extends MongoRepository<Listing, String> {
    Optional<Listing> findByListingID(String listingID);
}
