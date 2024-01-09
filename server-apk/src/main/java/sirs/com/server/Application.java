package sirs.com.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws NoSuchAlgorithmException, IOException {
        System.setProperty("javax.net.ssl.trustStore","server-apk/src/main/resources/truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword","niklas");
        CryptographicLibrary.generateAndStoreKeyPair("server-apk/src/main/resources/publicKeyServer.pubkey",
                "server-apk/src/main/resources/privateKeyServer.privkey");
        SpringApplication.run(Application.class, args);
    }
}