package sirs.com.server.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sirs.com.server.model.ClientAccount;
import sirs.com.server.service.AccountService;
import sirs.com.server.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    @Autowired
    private final UserService userService;

    @Autowired
    private final AccountService accountService;



    public UserController(UserService userService, AccountService accountService) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody String encryptedUserJsonString) throws Exception {
        return userService.loginUser(encryptedUserJsonString);
    }

    @PostMapping("/create-account")
    public ResponseEntity<List<String>> createUser(@RequestBody String encryptedUserJsonString) throws Exception {
        return userService.addUser(encryptedUserJsonString);
    }

    @GetMapping("/hasBankAccount/{username}")
    public ResponseEntity<Boolean> hasBankAccount(@PathVariable (value="username") String username) {
        return userService.hasBankAccount(username);
    }

    /*
    @GetMapping("/bankAccount/{username}")
    public ResponseEntity<ClientAccount> getBankAccount(@PathVariable (value="username") String username) {
        return accountService.getBankAccount(username);
    }

     */

    // i need an endpoint to get user by username
}