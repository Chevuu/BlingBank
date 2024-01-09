package main.java.sirs.com.crypto.CryptographicLibraryPackage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class UserData {
    private String encryptedUserData;
    private String digitalSignature;
    private String timestamp;
    private String publicKeyString;
    private String ivPublicKey;
    private String iv;

    public UserData(String encryptedUserData) {
        this.encryptedUserData = encryptedUserData;
    }

    public UserData(String encryptedUserData, String digitalSignature, String timestamp, String publicKeyString, String ivPublicKey, String iv) {
        this.encryptedUserData = encryptedUserData;
        this.digitalSignature = digitalSignature;
        this.timestamp = timestamp;
        this.publicKeyString = publicKeyString;
        this.ivPublicKey = ivPublicKey;
        this.iv = iv;
    }

    public UserData() {}

    public String getIvPublicKey() {
        return ivPublicKey;
    }

    public void setIvPublicKey(String ivPublicKey) {
        this.ivPublicKey = ivPublicKey;
    }

    public String getEncryptedUserData() {
        return encryptedUserData;
    }

    public void setEncryptedUserData(String encryptedUserData) {
        this.encryptedUserData = encryptedUserData;
    }

    public String getDigitalSignature() {
        return digitalSignature;
    }

    public void setDigitalSignature(String digitalSignature) {
        this.digitalSignature = digitalSignature;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public void setPublicKeyString(String publicKeyString) {
        this.publicKeyString = publicKeyString;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String convertUserDataToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static UserData convertJsonToUserData(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, UserData.class);
    }
}
