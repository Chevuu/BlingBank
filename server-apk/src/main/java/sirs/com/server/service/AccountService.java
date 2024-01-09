package sirs.com.server.service;

import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import sirs.com.server.model.Account;
import sirs.com.server.model.ClientAccount;
import sirs.com.server.model.Transaction;
import sirs.com.server.model.User;
import sirs.com.server.repository.AccountRepository;
import sirs.com.server.repository.UserRepository;

import javax.crypto.SecretKey;
import java.security.Key;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class AccountService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<String> getAccountByAccountHolder(@RequestBody String username) throws Exception {
        // check if user exists
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        User user = userOptional.get();

        if (user.getAccountId() == null) {
            return ResponseEntity.badRequest().build();
        }
        String sessionKeyString = user.getSessionKeyString();

        Optional<Account> accountOptional = this.accountRepository.findById(user.getAccountId());
        if(accountOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        else {
            Account account = accountOptional.get();
            ClientAccount clientAccount = new ClientAccount(account.getAccountHolders().toArray(new String[0]),
                    account.getBalance(), account.getCurrency(), account.getTransactions().toArray(new Transaction[0]));
            String accountJsonString = clientAccount.convertAccountToJsonString();
            Key sessionKey = CryptographicLibrary.convertStringToSessionKey(sessionKeyString);
            List<String> results = CryptographicLibrary.encryptJsonString(accountJsonString, sessionKey);
            Data returnData = new Data(results.get(0), results.get(1), "");
            returnData.setTimestamp(Instant.now().toString());
            PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile("server-apk/src/main/resources/privateKeyServer.privkey");
            String digitalSignature = CryptographicLibrary.createSignature(privateKey, results.get(0));
            returnData.setDigitalSignature(digitalSignature);
            String encryptedDataJsonString = returnData.convertDataToJsonString();
            return ResponseEntity.ok(encryptedDataJsonString);
        }
    }

    public ResponseEntity<String> createAccount(String data) throws Exception {
        Data myData = Data.convertJsonToData(data);
        String encryptedAccountJsonString = myData.getEncryptedData();
        String username = CryptographicLibrary.decryptUsernameFromData(myData);
        String iv = myData.getIv();

        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        User user = userOptional.get();

        String sessionKeyString = user.getSessionKeyString();
        SecretKey sessionKey = CryptographicLibrary.convertStringToSessionKey(sessionKeyString);

        String decryptedAccountJsonString = CryptographicLibrary.decryptDocumentWithAESKey(encryptedAccountJsonString, sessionKey, iv);
        ClientAccount clientAccount = ClientAccount.convertJsonToAccount(decryptedAccountJsonString);

        //TODO
        Account account = new Account(Arrays.asList(clientAccount.accountHolders), clientAccount.balance, clientAccount.currency, Arrays.asList(clientAccount.transactions));

        if(account.getAccountHolders().isEmpty()) {
            return ResponseEntity.badRequest().body("error");
        }
        List<String> accountHolders = account.getAccountHolders();
        for (String accountHolder : accountHolders) {
            Optional<User> currentUserOptional = userRepository.findByUsername(accountHolder);
            if(currentUserOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("There is no user for username" + accountHolder);
            }
            User currentUser = currentUserOptional.get();
            if (currentUser.getAccountId() != null) {
                return ResponseEntity.badRequest().body("User " + accountHolder + "already has an account!");
            }
        }

        for (String accountHolder : accountHolders) {
            Optional<User> currentUserOptional = userRepository.findByUsername(accountHolder);
            User currentUser = currentUserOptional.get();
            currentUser.setAccountId(account.getId());
            userRepository.deleteByUsername(accountHolder);
            //TODO check if currentUser is correct
            userRepository.save(currentUser);
        }

        accountRepository.save(account);
        return ResponseEntity.ok().build();
    }
}
