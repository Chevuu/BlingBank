package sirs.com.server.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import sirs.com.server.model.Transaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends MongoRepository<Transaction, String> {
    List<Transaction> findByUsername(String username);
}
