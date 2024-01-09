package main.java.sirs.com.crypto.CryptographicLibraryPackage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;

public class Data {
    private String encryptedData;
    private String iv;
    private String encryptedUsername;
    private String digitalSignature;
    private String timestamp;

    public Data(String encryptedData, String encryptedIV, String encryptedUsername) {
        this.encryptedData = encryptedData;
        this.iv = encryptedIV;
        this.encryptedUsername = encryptedUsername;
    }

    public Data() {}

    public String getEncryptedData() {
        return encryptedData;
    }

    public void setEncryptedData(String encryptedData) {
        this.encryptedData = encryptedData;
    }

    public String getIv() {
        return iv;
    }

    public void setIv(String iv) {
        this.iv = iv;
    }

    public String getEncryptedUsername() {
        return encryptedUsername;
    }

    public void setEncryptedUsername(String encryptedUsername) {
        this.encryptedUsername = encryptedUsername;
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

    public static  Data convertJsonToData(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Data.class);
    }

    public String convertDataToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }
}
