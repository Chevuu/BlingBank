package sirs.com.server.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import sirs.com.server.model.Account;
import sirs.com.server.model.ClientTransaction;
import sirs.com.server.model.Transaction;
import sirs.com.server.model.User;
import sirs.com.server.repository.AccountRepository;
import sirs.com.server.repository.TransactionRepository;
import sirs.com.server.repository.UserRepository;


import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.swing.text.html.Option;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    public ResponseEntity<String> getTransactionsByUsername(String username) throws Exception {
        //Data data = Data.convertJsonToData(encryptedDataJsonString);
        //String username = CryptographicLibrary.decryptUsernameFromData(data);

        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        User user = userOptional.get();

        PrivateKey privateKeyServer = CryptographicLibrary.readPrivateKeyFromFile("server-apk/src/main/resources/privateKeyServer.privkey");
        String sessionKeyString = user.getSessionKeyString();
        SecretKey sessionKey = CryptographicLibrary.convertStringToSessionKey(sessionKeyString);


        // get all transactions made by this user
        List<Transaction> transactions = transactionRepository.findByUsername(username);

        List<ClientTransaction> clientTransactions = new ArrayList<>();
        for (Transaction transaction : transactions) {
            clientTransactions.add(new ClientTransaction(transaction.username, transaction.transactionDate, transaction.value, transaction.description));
        }
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, sessionKey);
        byte[] iv = aesCipher.getIV(); // Store this IV for decryption
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        List<String> encryptedTransactionsJsonString = new ArrayList<>();
        for(ClientTransaction transaction : clientTransactions) {
            String transactionJsonString = transaction.convertTransactionToJsonString();
            String encryptedTransactionJsonString = CryptographicLibrary.encryptTransactionJsonString(transactionJsonString, aesCipher);
            encryptedTransactionsJsonString.add(encryptedTransactionJsonString);
        }

        ObjectMapper objectMapper = new ObjectMapper();
        Data returnData = new Data(objectMapper.writeValueAsString(encryptedTransactionsJsonString), ivBase64, "");
        String temp = objectMapper.writeValueAsString(encryptedTransactionsJsonString);
        String signature = CryptographicLibrary.createSignature(privateKeyServer, objectMapper.writeValueAsString(encryptedTransactionsJsonString));
        returnData.setDigitalSignature(signature);
        Instant timestamp = Instant.now();
        returnData.setTimestamp(timestamp.toString());
        String returnDataJsonString = returnData.convertDataToJsonString();

        return ResponseEntity.ok(returnDataJsonString);
        }

    public ResponseEntity<String> addTransaction(String encryptedDataJsonString, String username)
            throws Exception {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong username.");
        }
        User user = userOptional.get();
        String publicKeyString = user.getPublicKeyString();

        PublicKey publicKey = CryptographicLibrary.convertStringToPublicKey(publicKeyString);

        String sessionKeyString = user.getSessionKeyString();
        SecretKey sessionKey = CryptographicLibrary.convertStringToSessionKey(sessionKeyString);

        String decryptedTransactionJsonString = CryptographicLibrary.decryptDataJsonString(encryptedDataJsonString, publicKey, sessionKey);

        Data data = Data.convertJsonToData(encryptedDataJsonString);
        String digitalSignature = data.getDigitalSignature();


        ClientTransaction clientTransaction = ClientTransaction.convertJsonToTransaction(decryptedTransactionJsonString);
        Transaction transaction = new Transaction(clientTransaction.username, clientTransaction.transactionDate, clientTransaction.value, clientTransaction.description);
        transaction.setDigitalSignature(digitalSignature);

        if(transaction.getUsername() == null) {
            return ResponseEntity.badRequest().build();
        }
        else {
            // store transaction in the database
            String id = user.getAccountId();
            Optional<Account> accountOptional = accountRepository.findById(id);
            if(accountOptional.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            Account account = accountOptional.get();
            double oldBalance = account.getBalance();
            double newBalance = oldBalance + transaction.getValue();
            account.setBalance(newBalance);
            accountRepository.deleteById(id);
            accountRepository.save(account);
            transactionRepository.save(transaction);
            return ResponseEntity.ok().build();
        }

    }
}
