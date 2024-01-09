package test.java.sirs.com.crypto;

import com.google.gson.JsonObject;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import org.junit.jupiter.api.Test;


import static org.junit.jupiter.api.Assertions.*;



public class CryptographicLibraryTest {

    private static final String unprotectedDocumentPath = "Documents/account.json";
    private static final String protectedDocumentPath = "Documents/protected_account.json";

    @Test
    public void checkEncryptDecryptTest() throws Exception {

        // original version
        JsonObject inputDocument = CryptographicLibrary.readJsonFromFile(unprotectedDocumentPath);

        // encrypt
        CryptographicLibrary.encryptDocument(unprotectedDocumentPath, protectedDocumentPath);

        // decrypt
        CryptographicLibrary.decryptDocument(protectedDocumentPath, unprotectedDocumentPath);

        // after encryption and decryption
        JsonObject inputDocumentAfter = CryptographicLibrary.readJsonFromFile(unprotectedDocumentPath);

        assertEquals(inputDocument, inputDocumentAfter);
    }

    @Test
    public void checkDocumentIntegrityAndFreshnessTest() throws Exception {
        JsonObject inputDocument = CryptographicLibrary.readJsonFromFile(unprotectedDocumentPath);

        assertThrows(NullPointerException.class ,() -> CryptographicLibrary.checkDocumentIntegrityAndFreshness(unprotectedDocumentPath));

        CryptographicLibrary.encryptDocument(unprotectedDocumentPath, protectedDocumentPath);

        assertTrue(CryptographicLibrary.checkDocumentIntegrityAndFreshness(protectedDocumentPath));
    }

    @Test
    public void checkDocumentTamperDetection() throws Exception {
        JsonObject protectedDocument = CryptographicLibrary.readJsonFromFile(protectedDocumentPath);
        String encryptedData = protectedDocument.get("encryptedData").getAsString();
        encryptedData += "ABCDEF";
        protectedDocument.remove("encryptedData");
        protectedDocument.addProperty("encryptedData", encryptedData);

        CryptographicLibrary.createJsonFile(protectedDocument, "Documents/tampered_account.json", true);

        assertFalse(CryptographicLibrary.checkDocumentIntegrityAndFreshness("Documents/tampered_account.json"));
    }


}