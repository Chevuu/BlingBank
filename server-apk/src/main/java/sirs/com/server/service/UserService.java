package sirs.com.server.service;

import main.java.sirs.com.crypto.CryptographicLibraryPackage.UserData;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import sirs.com.server.model.ClientAccount;
import sirs.com.server.model.User;
import sirs.com.server.repository.UserRepository;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;

import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;

import javax.crypto.SecretKey;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    Logger log;
    @Autowired
    private UserRepository userRepository;

    public ResponseEntity<String> loginUser(String userDataJsonString) throws Exception {
        // decrpyt the user login data using rsa because no session key has been generated yes

        String decryptedUserJsonString = CryptographicLibrary.decryptUserDataJsonString(userDataJsonString);
        if (decryptedUserJsonString.isEmpty()) {
            return ResponseEntity.badRequest().build(); //tampered user data
        }
        User user = User.convertJsonToUser(decryptedUserJsonString);

        Optional<User> userOptional = userRepository.findByUsername(user.getUsername());
        if(userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Wrong username.");
        }
        User userDatabase = userOptional.get();

        if (user.equals(userDatabase)) {
            this.printServiceCall();
            if(userDatabase.getPassword() == null) {
                return ResponseEntity.badRequest().build();
            }
            else {

                String publicKeyString = userDatabase.getPublicKeyString();
                PublicKey publicKey = CryptographicLibrary.convertStringToPublicKey(publicKeyString);
                SecretKey sessionKey = CryptographicLibrary.generateAESKey();

                String sessionKeyString = CryptographicLibrary.convertSessionKeyToString(sessionKey);
                userDatabase.setSessionKeyString(sessionKeyString);
                userRepository.deleteByUsername(userDatabase.getUsername());
                userRepository.save(userDatabase);
                String encryptedSessionKey = CryptographicLibrary.encryptAESKeyWithPublicKey(sessionKey, publicKey);
                return ResponseEntity.ok(encryptedSessionKey);
            }
        } else {
            System.out.println("login error on server");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed");

        }
    }

    public ResponseEntity<List<String>> addUser(String encryptedUserDataJsonString) throws Exception {
        UserData userData = UserData.convertJsonToUserData(encryptedUserDataJsonString);
        String ivPublicKey = userData.getIvPublicKey();
        String encryptedPublicKeyUser = userData.getPublicKeyString();
        String symmetricKeyPath = "server-apk/src/main/resources/symmetricKey.key";
        PublicKey publicKey = CryptographicLibrary.decryptPublicKey(encryptedPublicKeyUser, ivPublicKey, symmetricKeyPath);
        String publicKeyString = CryptographicLibrary.convertPublicKeyToByteArray(publicKey);
        String decryptedUserJsonString = CryptographicLibrary.decryptUserDataJsonString(encryptedUserDataJsonString);
        if (decryptedUserJsonString.isEmpty()) {
            return ResponseEntity.badRequest().build(); //tampered user data
        }
        User user = User.convertJsonToUser(decryptedUserJsonString);
        user.setPublicKeyString(publicKeyString);
        userRepository.save(user);

        List<String> encryptedPublicKeyAndIV = CryptographicLibrary.encryptPublicKey("server-apk/src/main/resources/publicKeyServer.pubkey",
                "server-apk/src/main/resources/symmetricKey.key");

        return ResponseEntity.ok(encryptedPublicKeyAndIV); }

    public ResponseEntity<String> deleteUser(String data) throws Exception {
        Data myData = Data.convertJsonToData(data);

        String encryptedUsername = myData.getEncryptedUsername();
        PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile("server-apk/src/main/resources/privateKeyServer.privkey");
        String decryptedUsernameJsonString = CryptographicLibrary.decryptUsernameJsonString(encryptedUsername, privateKey);

        userRepository.deleteByUsername(decryptedUsernameJsonString);

        return ResponseEntity.ok().build();
    }

    public void printServiceCall() {
        System.out.println("User service called");
    }

    public ResponseEntity<Boolean> hasBankAccount(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isEmpty()) {
            return ResponseEntity.ok(false);
        }
        User user = userOptional.get();
        if (user.getAccountId() == null) {
            return ResponseEntity.ok(false);
        }
        return ResponseEntity.ok(true);



    }
}
