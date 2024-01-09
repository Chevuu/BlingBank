package sirs.com.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.UserData;
import sirs.com.Main;
import sirs.com.models.User;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;



import static sirs.com.controller.SharedController.buildPostRequest;
import static sirs.com.controller.SharedController.setNewScreen;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Text errorText;

    @FXML
    protected void login() throws Exception {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (validateInput("Username", username) && validateInput("Password", password)) {
             sendLoginRequest(username, password);
        }
    }

    @FXML
    protected void createAccount() {
        if(setNewScreen("create-account")) {
            System.out.println("User is now on create account page.");
        }
    }

    private void sendLoginRequest(String username, String password) throws Exception {
        String url = "http://192.168.0.10:80/api/login";
        // String url = "http://localhost:8080/api/login";
        String publicKeyPath = "client-apk/src/main/resources/" + username + "Public.pubkey";
        String privateKeyPath = "client-apk/src/main/resources/" + username + "Private.privkey";

        PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile(privateKeyPath);
        UserData userData = CryptographicLibrary.encryptUserDataJsonString(username, password, publicKeyPath, privateKey);
        String userDataJsonString = userData.convertUserDataToJsonString();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildPostRequest(url, userDataJsonString);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode();
                    String responseBody = response.body();
                    return new Pair<>(statusCode, responseBody);
                })
                .thenAccept(pair -> handleLoginResponse(pair.getKey(), pair.getValue(), username))
                .exceptionally(this::handleLoginFailure);
    }

    private void handleLoginResponse(int statusCode, String encryptedSessionKey, String username) {
        if (statusCode == 200) {
            Platform.runLater(() -> {
                if(setNewScreen("dashboard")) {
                    System.out.println("Login successful for user: " + username);
                    Main.currentUser = username;

                    try {
                        String privateKeyPath = "client-apk/src/main/resources/" + username + "Private.privkey";
                        PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile(privateKeyPath);
                        Key sessionKey = CryptographicLibrary.decryptAESKeyWithPrivateKey(encryptedSessionKey, privateKey);
                        CryptographicLibrary.writeSymmetricKey(sessionKey, "client-apk/src/main/resources/sessionKey.key");
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } else {
            Platform.runLater(() -> {
                errorText.setText("Login failed.");
                showErrorTextTemporarily();
            });
        }
    }

    private Void handleLoginFailure(Throwable e) {
        System.out.println("An error occurred during login: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("An error occurred.");
            showErrorTextTemporarily();
        });
        return null;
    }

    private void showErrorTextTemporarily() {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }

    private boolean validateInput(String field, String content) {
        if (content.isEmpty()) {
            errorText.setText(field + " is empty.");
            showErrorTextTemporarily();
            return false;
        }
        return true;
    }
}
