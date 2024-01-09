package sirs.com.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sirs.com.server.model.Transaction;
import sirs.com.server.model.User;
import sirs.com.server.service.TransactionService;
import sirs.com.server.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/transaction")
public class TransactionController {
    @Autowired
    private TransactionService transactionService;
    @GetMapping("/getAll/{username}")
    public ResponseEntity<String> getTransactionsByUsername(@PathVariable (value="username") String username)
            throws Exception {
        return transactionService.getTransactionsByUsername(username);
    }

    @PostMapping("/add/{username}")
    public ResponseEntity<String> addTransaction(@RequestBody String encryptedDataJsonString, @PathVariable (value="username") String username) throws Exception {
        return transactionService.addTransaction(encryptedDataJsonString, username);
    }
}
