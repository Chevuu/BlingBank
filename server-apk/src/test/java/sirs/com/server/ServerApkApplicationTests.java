package sirs.com.server;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import sirs.com.server.model.User;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class ServerApkApplicationTests {

    @Test
    void contextLoads() {
    }

    /*
    @Test
    public void checkDocumentIntegrityAndFreshnessTest2() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPairClient = keyGen.generateKeyPair();
        PublicKey publicKeyClient = keyPairClient.getPublic();

        KeyPair keyPairServer = keyGen.generateKeyPair();
        PrivateKey privateKeyServer = keyPairServer.getPrivate();

        User user = new User("Niklas", "passwort");
        String userJsonString = user.convertUserToJsonString();
        String encryptedUserJsonString = CryptographicLibrary.encryptDocumentJsonString(userJsonString, privateKeyServer);
        assertTrue(CryptographicLibrary.checkDocumentJsonStringIntegrityAndFreshness(encryptedUserJsonString, publicKeyClient));
    }

     */

}
