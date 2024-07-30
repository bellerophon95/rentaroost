package com.kashmira.listingsservice.repository;

import com.kashmira.listingsservice.models.Listing;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ReactiveListingRepository extends ReactiveMongoRepository<Listing, String> {
}
