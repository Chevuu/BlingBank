package main.java.sirs.com.crypto.CryptographicLibraryPackage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Objects;

public class User {
    private String username;
    private String password;
    private String publicKeyString;
    private String sessionKeyString;
    private String accountId;

    public User() {}

    public User(String username, String password, String publicKeyString, String sessionKeyString) {
        this.username = username;
        this.password = password;
        this.publicKeyString = publicKeyString;
        this.sessionKeyString = sessionKeyString;
    }

    public String getPublicKeyString() {
        return publicKeyString;
    }

    public void setPublicKeyString(String publicKeyString) {
        this.publicKeyString = publicKeyString;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getSessionKeyString() {
        return sessionKeyString;
    }

    public void setSessionKeyString(String sessionKeyString) {
        this.sessionKeyString = sessionKeyString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(username, user.username) && Objects.equals(password, user.password);
    }

    public String convertUserToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static User convertJsonToUser(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, User.class);
    }


}
