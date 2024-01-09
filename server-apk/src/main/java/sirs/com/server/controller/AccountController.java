package sirs.com.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sirs.com.server.model.Account;
import sirs.com.server.model.User;
import sirs.com.server.repository.AccountRepository;
import sirs.com.server.service.AccountService;

import java.util.Optional;

@RestController
@RequestMapping("/api/account")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @GetMapping("/get/{username}")
    public ResponseEntity<String> getAccount(@PathVariable (value="username") String username) throws Exception {
        return this.accountService.getAccountByAccountHolder(username);
    }

    @PostMapping("/create")
    public ResponseEntity<String> createAccount(@RequestBody String encryptedAccountJsonString) throws Exception {
        return this.accountService.createAccount(encryptedAccountJsonString);
    }
}
