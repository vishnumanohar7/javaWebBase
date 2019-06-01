package com.piotics.Repository;

import com.piotics.model.ApplicationUser;

import java.util.Optional;

//import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApplicationUserMongoRepository extends MongoRepository<ApplicationUser, Integer> {
	ApplicationUser findByUsername(String username);

	Optional<ApplicationUser> findById(String id);
}
