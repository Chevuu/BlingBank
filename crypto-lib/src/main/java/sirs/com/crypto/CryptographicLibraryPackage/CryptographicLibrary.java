package main.java.sirs.com.crypto.CryptographicLibraryPackage;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.*;

public class CryptographicLibrary {

    private final static int FIVE_MINUTES_IN_SECONDS = 300;

    public static boolean checkDocumentIntegrityAndFreshness(String protectedDocumentPath) throws Exception {
        JsonObject protectedDocument = readJsonFromFile(protectedDocumentPath);
        String signature = protectedDocument.get("signature").getAsString();
        String nonce = protectedDocument.get("nonce").getAsString();

        PublicKey publicKey = readPublicKeyFromFile("keys/alice.pubkey");
        String timestampString = protectedDocument.get("timestamp").getAsString();
        Instant timestamp = Instant.parse(timestampString);

        protectedDocument.remove("signature");

        // verify the signature
        boolean rightSignature = verifyDocumentSignature(protectedDocument.toString(), signature, publicKey);

        if (!rightSignature) {
            System.out.println("Document has been tampered with!");
            return false;
        }

        //verify timestamp
        if(!withinOneMinute(timestamp)) {
            return false;
        }

        // verify nonce
        String nonceStoragePath = "crypto-lib/src/used_nonces.txt";
        boolean isFresh = verifyDocumentFreshness(nonce, nonceStoragePath);

        if (!isFresh) {
            System.out.println("Document is not fresh!");
            return false;
        }

        return true;
    }

    public static boolean checkDocumentJsonStringIntegrityAndFreshness(String jsonString, String signature, String timestampString, PublicKey publicKeySender) throws Exception {
        // JsonParser parser = new JsonParser();
        // JsonObject protectedDocument = parser.parse(jsonString).getAsJsonObject();


        //String nonce = protectedDocument.get("nonce").getAsString();
        Instant timestamp = Instant.parse(timestampString);

        // verify the signature
        boolean rightSignature = verifyDocumentSignature(jsonString, signature, publicKeySender);

        if (!rightSignature) {
            System.out.println("Document has been tampered with!");
            return false;
        }

        //verify timestamp
        if(!withinOneMinute(timestamp)) {
            return false;
        }

        // verify nonce
        /*
        String nonceStoragePath = "crypto-lib/src/used_nonces.txt";
        boolean isFresh = verifyDocumentFreshness(nonce, nonceStoragePath);

        if (!isFresh) {
            System.out.println("Document is not fresh!");
            return false;
        }

         */

        return true;
    }

    public static boolean withinOneMinute(Instant timestamp) {
        Instant now = Instant.now();
        long difference = Math.abs(timestamp.toEpochMilli() - now.toEpochMilli());
        return difference <= 60000; // 60,000 milliseconds in one minute
    }

    public static String decryptDocument(String protectedDocumentPath, String unprotectedDocumentPath) throws Exception {
        PrivateKey privateKey = readPrivateKeyFromFile("keys/bob.privkey");

        JsonObject protectedDocument = readJsonFromFile(protectedDocumentPath);

        String encryptedDataBase64 = protectedDocument.get("encryptedData").getAsString();
        String encryptedKeyBase64 = protectedDocument.get("encryptedAESKey").getAsString();
        String iv = protectedDocument.get("iv").getAsString();

        // decrypt document
        SecretKey aesKey = decryptAESKeyWithPrivateKey(encryptedKeyBase64, privateKey);
        String decryptedDocument = decryptDocumentWithAESKey(encryptedDataBase64, aesKey, iv);
        JsonObject decryptedJsonObject = JsonParser.parseString(decryptedDocument).getAsJsonObject();

        createJsonFile(decryptedJsonObject, unprotectedDocumentPath, true);
        return unprotectedDocumentPath;
    }

    public static String encryptDocument(String inputDocumentPath, String outputDocumentPath) throws Exception {

        JsonObject inputDocument = readJsonFromFile(inputDocumentPath);

        PrivateKey privateKeyServer = readPrivateKeyFromFile("keys/alice.privkey");
        PublicKey publicKeyClient = readPublicKeyFromFile("keys/bob.pubkey");

        // create nonce
        String nonce = createNonce();

        // encrypt document
        SecretKey aesKey = generateAESKey();
        String encryptedAESKey = encryptAESKeyWithPublicKey(aesKey, publicKeyClient);
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(inputDocument);
        List<String> result = encrpytDocumentWithAES(aesKey, jsonString);
        String encryptedData = result.get(0);
        String iv = result.get(1);

        JsonObject protectedDocument = new JsonObject();

        protectedDocument.addProperty("encryptedData", encryptedData);
        protectedDocument.addProperty("encryptedAESKey", encryptedAESKey);
        protectedDocument.addProperty("iv", iv);
        protectedDocument.addProperty("nonce", nonce);

        // Add timestamp
        Instant timestamp = Instant.now();
        protectedDocument.addProperty("timestamp", timestamp.toString());


        String secureDocumentString = new GsonBuilder().setPrettyPrinting().create().toJson(protectedDocument);

        // create signature
        String signature = createSignature(privateKeyServer, secureDocumentString);
        protectedDocument.addProperty("signature", signature);

        String protectedDocumentPath = createJsonFile(protectedDocument, outputDocumentPath, true);
        return protectedDocumentPath;
    }

    public static JsonObject readJsonFromFile(String filePath) {
        try {
            String jsonContent = new String(Files.readAllBytes(Paths.get(filePath)));
            JsonParser parser = new JsonParser();
            return parser.parse(jsonContent).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String createJsonFile(JsonObject jsonObject, String fileName, boolean prettyPrint) {
        Gson gson = prettyPrint ? new GsonBuilder().setPrettyPrinting().create() : new Gson();
        Path filePath = Paths.get(fileName);

        try (FileWriter file = new FileWriter(filePath.toFile())) {
            gson.toJson(jsonObject, file);
            return filePath.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static boolean verifyDocumentFreshness(String nonce, String nonceStoragePath) throws IOException {
        Path noncePath = Paths.get(nonceStoragePath);
        List<String> usedNonces = Files.readAllLines(noncePath, StandardCharsets.UTF_8);
        ListIterator<String> nonceIterator = usedNonces.listIterator();
        Instant fiveMinutesAgo = Instant.now().minusSeconds(FIVE_MINUTES_IN_SECONDS);

        // Check nonces from the last 5 minutes
        while (nonceIterator.hasPrevious()) {
            String currentNonce = nonceIterator.previous();
            String[] parts = currentNonce.split("\\|");
            Instant timestamp = Instant.parse(parts[0]);
            // if no nonce is found up til 5 minutes in the past, the document can be considered fresh
            // if a nonce is found, the document isn't considered fresh
            if (timestamp.isBefore(fiveMinutesAgo)) {
                Files.write(noncePath, (nonce + System.lineSeparator()).getBytes(
                        StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);
                System.out.println("Document is fresh!!!!");
                return true;
            } else if (nonce.equals(parts[1])) {
                System.out.println("Not fresh!!!!");
                return false;
            }
        }
        System.out.println("Document is fresh!!!!");
        return true;
    }

    private static boolean verifyDocumentSignature(String jsonString, String signatureString, PublicKey publicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        byte[] signatureBytes = Base64.getDecoder().decode(signatureString);

        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initVerify(publicKey);
        // String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(jsonObject);
        signatureInstance.update(jsonString.getBytes(StandardCharsets.UTF_8));

        // Verify the signature
        boolean isSignatureValid = signatureInstance.verify(signatureBytes);
        if (isSignatureValid) {
            System.out.println("Signature is valid. Document is authentic and unchanged.");
            return true;
        } else {
            System.out.println("Signature is invalid. Document may have been tampered with.");
            return false;
        }
    }

    public static String decryptDocumentWithAESKey(String encryptedDocumentBase64, SecretKey aesKey, String ivBase64) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        byte[] iv = Base64.getDecoder().decode(ivBase64);
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.DECRYPT_MODE, aesKey, new IvParameterSpec(iv));
        byte[] encryptedDocument = Base64.getDecoder().decode(encryptedDocumentBase64);
        byte[] decryptedData = aesCipher.doFinal(encryptedDocument);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static String decryptJsonString(String encryptedDataBase64, String ivBase64, Key symmetricKey) throws Exception {
        byte[] encryptedData = Base64.getDecoder().decode(encryptedDataBase64);
        byte[] iv = Base64.getDecoder().decode(ivBase64);

        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        aesCipher.init(Cipher.DECRYPT_MODE, symmetricKey, ivParameterSpec);

        byte[] decryptedData = aesCipher.doFinal(encryptedData);
        return new String(decryptedData, StandardCharsets.UTF_8);
    }

    public static String encryptTransactionJsonString(String jsonString, Cipher aesCipher) throws Exception{
        byte[] encryptedData = aesCipher.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
        String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData);
        return encryptedDataBase64;
    }

    public static List<String> encryptJsonString(String jsonString, Key symmetricKey) throws Exception {
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, symmetricKey);
        byte[] iv = aesCipher.getIV(); // Store this IV for decryption
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        byte[] encryptedData = aesCipher.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
        String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData);
        List<String> ret = new ArrayList<>();
        ret.add(encryptedDataBase64);
        ret.add(ivBase64);
        return ret;
    }

    public static String decryptUsernameJsonString(String encryptedUsernameJsonString, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] encryptedBytes = Base64.getDecoder().decode(encryptedUsernameJsonString);
        byte[] decryptedBytes = rsaCipher.doFinal(encryptedBytes);
        return new String(decryptedBytes, StandardCharsets.UTF_8);
    }

    public static String decryptUsernameFromData(Data data) throws Exception {
        String encryptedUsername = data.getEncryptedUsername();
        PrivateKey privateKey = readPrivateKeyFromFile("server-apk/src/main/resources/privateKeyServer.privkey");
        return decryptUsernameJsonString(encryptedUsername, privateKey);
    }

    public static String encryptAESKeyWithPublicKey(SecretKey aesKey, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesKey.getEncoded());
        return Base64.getEncoder().encodeToString(encryptedAesKey);
    }

    public static SecretKey decryptAESKeyWithPrivateKey(String encryptedAESKey, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] encryptedKeyBytes = Base64.getDecoder().decode(encryptedAESKey);

        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.DECRYPT_MODE, privateKey);

        byte[] decryptedKeyBytes = rsaCipher.doFinal(encryptedKeyBytes);

        // Reconstructing the SecretKey object from the decrypted bytes
        return new SecretKeySpec(decryptedKeyBytes, "AES");
    }


    public static String encryptUserWithPublicKey(String userJsonString, PublicKey publicKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        rsaCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(userJsonString.getBytes());
        return Base64.getEncoder().encodeToString(encryptedAesKey);
    }

    private static List<String> encrpytDocumentWithAES(SecretKey aesKey, String jsonString) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cipher aesCipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        aesCipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] iv = aesCipher.getIV(); // Store this IV for decryption
        String ivBase64 = Base64.getEncoder().encodeToString(iv);

        byte[] encryptedData = aesCipher.doFinal(jsonString.getBytes(StandardCharsets.UTF_8));
        String encryptedDataBase64 = Base64.getEncoder().encodeToString(encryptedData);
        List<String> ret = new ArrayList<>();
        ret.add(encryptedDataBase64);
        ret.add(ivBase64);
        return ret;
    }

    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256);
        return keyGen.generateKey();
    }

    public static String createNonce() {
        SecureRandom random = new SecureRandom();
        byte[] nonceBytes = new byte[16];
        random.nextBytes(nonceBytes);
        String nonce = Base64.getEncoder().encodeToString(nonceBytes);
        String timestamp = String.valueOf(Instant.now());
        return timestamp + '|' + nonce;
    }

    //@TODO(Kevin): How to generate keys in a secret way, describe how keys are distributed
    public static String createSignature(PrivateKey privateKey, String jsonString) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException {
        Signature signatureInstance = Signature.getInstance("SHA256withRSA");
        signatureInstance.initSign(privateKey);
        signatureInstance.update(jsonString.getBytes(StandardCharsets.UTF_8));
        byte[] signature = signatureInstance.sign();
        return Base64.getEncoder().encodeToString(signature);
    }

    private static byte[] readFile(String path) throws IOException {
        FileInputStream fis = new FileInputStream(path);
        byte[] content = new byte[fis.available()];
        fis.read(content);
        fis.close();
        return content;
    }

    public static Key readSecretKey(String secretKeyPath) throws Exception {
        byte[] encoded = readFile(secretKeyPath);
        return new SecretKeySpec(encoded, "AES");
    }

    public static PublicKey readPublicKeyFromFile(String publicKeyPath) throws Exception {
        System.out.println("Reading public key from file " + publicKeyPath + " ...");
        byte[] pubEncoded = readFile(publicKeyPath);
        X509EncodedKeySpec pubSpec = new X509EncodedKeySpec(pubEncoded);
        KeyFactory keyFacPub = KeyFactory.getInstance("RSA");
        return keyFacPub.generatePublic(pubSpec);
    }

    public static PublicKey convertStringToPublicKey(String publicKeyStr) throws Exception {
        byte[] decodedKey = Base64.getDecoder().decode(publicKeyStr);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Or the appropriate algorithm
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decodedKey);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static String convertPublicKeyToByteArray(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    public static SecretKey convertStringToSessionKey(String sessionKeyStr) {
        byte[] decodedKey = Base64.getDecoder().decode(sessionKeyStr);
        return new SecretKeySpec(decodedKey, "AES");
    }

    public static String convertSessionKeyToString(SecretKey sessionKey) {
        return Base64.getEncoder().encodeToString(sessionKey.getEncoded());
    }

    public static PrivateKey readPrivateKeyFromFile(String privateKeyPath) throws Exception {
        byte[] privEncoded = readFile(privateKeyPath);
        PKCS8EncodedKeySpec privSpec = new PKCS8EncodedKeySpec(privEncoded);
        KeyFactory keyFacPriv = KeyFactory.getInstance("RSA");
        return keyFacPriv.generatePrivate(privSpec);
    }

    public static Key readSymmetricKey(String keyPath) throws Exception {
        FileInputStream fis = new FileInputStream(keyPath);
        byte[] encoded = new byte[fis.available()];
        fis.read(encoded);
        fis.close();
        SecretKeySpec keySpec = new SecretKeySpec(encoded, "AES");

        return keySpec;
    }

    public static void writeSymmetricKey(Key symmetricKey, String keyPath) throws IOException {
        byte[] encoded = symmetricKey.getEncoded();
        FileOutputStream fos = new FileOutputStream(keyPath);
        fos.write(encoded);
        fos.close();
    }

    public static void generateAndStoreKeyPair(String publicKeyPath, String privateKeyPath) throws NoSuchAlgorithmException, IOException {
        File publicKeyFile = new File(publicKeyPath);
        File privateKeyFile = new File(privateKeyPath);

        if (!publicKeyFile.exists() || !privateKeyFile.exists()) {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);
            KeyPair keyPair = keyGen.generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();

            byte[] publicKeyEncoded = publicKey.getEncoded();
            writeFile(publicKeyPath, publicKeyEncoded);

            byte[] privateKeyEncoded = privateKey.getEncoded();
            writeFile(privateKeyPath, privateKeyEncoded);
        } else {
            System.out.println("Keys already exist. Skipping key generation.");
        }
    }

    private static void writeFile(String path, byte[] content) throws FileNotFoundException, IOException {
        FileOutputStream fos = new FileOutputStream(path);
        fos.write(content);
        fos.close();
    }

    public static List<String> encryptPublicKey(String publicKeyPath, String symmetricKeyPath) throws Exception{
        PublicKey publicKey = readPublicKeyFromFile(publicKeyPath);
        Key symmetricKey = readSymmetricKey(symmetricKeyPath);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        // generate the Initialization Vector
        byte[] iv = new byte[16]; // AES block size is 16 bytes
        new SecureRandom().nextBytes(iv);
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, symmetricKey, ivParameterSpec);
        byte[] cipherBytes = cipher.doFinal(publicKey.getEncoded());

        JsonObject json = new JsonObject();
        json.addProperty("encryptedPublicKey", Base64.getEncoder().encodeToString(cipherBytes));
        json.addProperty("iv", Base64.getEncoder().encodeToString(iv));

        List<String> results = new ArrayList<>();
        results.add(Base64.getEncoder().encodeToString(cipherBytes));
        results.add(Base64.getEncoder().encodeToString(iv));
        return results;
    }

    public static PublicKey decryptPublicKey(String encryptedPublicKey, String ivString, String symmetricKeyPath) throws Exception {
        byte[] encryptedKey = Base64.getDecoder().decode(encryptedPublicKey);
        byte[] iv = Base64.getDecoder().decode(ivString);

        Key symmetricKey = readSymmetricKey(symmetricKeyPath);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, symmetricKey, ivParameterSpec);

        byte[] decryptedKey = cipher.doFinal(encryptedKey);

        // Convert the decrypted key bytes back to a PublicKey
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // Assuming it's an RSA key
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(decryptedKey);
        return keyFactory.generatePublic(publicKeySpec);
    }

    public static List<String> decryptTransactionsJsonString(String dataJsonString) throws Exception {
        //TODO use session key
        Data data = Data.convertJsonToData(dataJsonString);
        Key sessionKey = readSymmetricKey("client-apk/src/main/resources/sessionKey.key");
        String encryptedDataJsonString = data.getEncryptedData();
        String digitalSignature = data.getDigitalSignature();
        String timestamp = data.getTimestamp();
        String iv = data.getIv();
        PublicKey publicKeyServer = readPublicKeyFromFile("server-apk/src/main/resources/publicKeyServer.pubkey");
        boolean result = checkDocumentJsonStringIntegrityAndFreshness(encryptedDataJsonString, digitalSignature, timestamp, publicKeyServer);
        if (!result) {
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> encryptedTransactions = objectMapper.readValue(encryptedDataJsonString, new TypeReference<List<String>>() {});
        List<String> decryptedTransactions = new ArrayList<>();
        for(String encryptedTransaction : encryptedTransactions) {
            String decryptedTransaction = decryptJsonString(encryptedTransaction, iv, sessionKey);
            decryptedTransactions.add(decryptedTransaction);
        }
        return decryptedTransactions;
    }


    public static String decryptUserDataJsonString(String userDataJsonString) throws Exception {
        UserData userData = UserData.convertJsonToUserData(userDataJsonString);
        Key symmetricKey = readSymmetricKey("server-apk/src/main/resources/symmetricKey.key");
        String symmetricKeyPath = "server-apk/src/main/resources/symmetricKey.key";
        // PrivateKey privateKeyServer = CryptographicLibrary.readPrivateKeyFromFile("server-apk/src/main/resources/privateKeyServer.privkey");
        String encryptedUserJsonString = userData.getEncryptedUserData();
        String digitalSignature = userData.getDigitalSignature();
        String timestamp = userData.getTimestamp();
        String publicKeyUserString = userData.getPublicKeyString();
        String ivPublicKey = userData.getIvPublicKey();
        String iv = userData.getIv();
        PublicKey publicKeyUser = decryptPublicKey(publicKeyUserString, ivPublicKey, symmetricKeyPath);
        boolean result = checkDocumentJsonStringIntegrityAndFreshness(encryptedUserJsonString, digitalSignature, timestamp, publicKeyUser);
        if (!result) {
            return "";
        }
        String decryptedJsonString =  decryptJsonString(encryptedUserJsonString, iv, symmetricKey);
        return decryptedJsonString;
    }

    public static UserData encryptUserDataJsonString(String username, String password, String publicKeyPath, PrivateKey privateKey) throws Exception {
        Key symmetricKey = readSymmetricKey("server-apk/src/main/resources/symmetricKey.key");
        String symmetricKeyPath = "server-apk/src/main/resources/symmetricKey.key";

        List<String> encryptedKeyAndIV = encryptPublicKey(publicKeyPath, symmetricKeyPath);
        String encryptedPublicKey = encryptedKeyAndIV.get(0);
        String ivPublicKey = encryptedKeyAndIV.get(1);

        User user = new User(username, password, encryptedPublicKey, "");
        String userString = user.convertUserToJsonString();

        List<String> encrypted = encryptJsonString(userString, symmetricKey);
        String encryptedUserJsonString = encrypted.get(0);
        String iv = encrypted.get(1);

        String digitalSignature = createSignature(privateKey, encryptedUserJsonString);

        Instant timestamp = Instant.now();

        UserData userData = new UserData(encryptedUserJsonString, digitalSignature, timestamp.toString(), encryptedPublicKey, ivPublicKey, iv);
        return userData;
    }

    public static String decryptDataJsonString(String encryptedDataJsonString, PublicKey publicKeyUser, Key symmetricKey) throws Exception {
        Data data = Data.convertJsonToData(encryptedDataJsonString);
        String encryptedData = data.getEncryptedData();
        String digitalSignature = data.getDigitalSignature();
        String timestamp = data.getTimestamp();
        String iv = data.getIv();
        boolean result = checkDocumentJsonStringIntegrityAndFreshness(encryptedData, digitalSignature, timestamp, publicKeyUser);
        if (!result) {
            return "";
        }
        String decryptedJsonString =  decryptJsonString(encryptedData, iv, symmetricKey);
        return decryptedJsonString;
    }
}
