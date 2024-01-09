package sirs.com.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import sirs.com.server.model.Account;

import java.util.Optional;

@Repository
public interface AccountRepository extends MongoRepository<Account, String> {
    Optional<Account> findById(String id);
}
