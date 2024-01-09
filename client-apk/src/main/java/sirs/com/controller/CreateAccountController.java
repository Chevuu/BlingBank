package sirs.com.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.UserData;
import sirs.com.Main;
import sirs.com.models.User;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.PrivateKey;
import java.security.PublicKey;


public class CreateAccountController {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private Text errorText;

    @FXML
    protected void createAccount() throws Exception {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String password2 = passwordField.getText();

        if (username.isEmpty() || password.isEmpty() || password2.isEmpty()) {
            errorText.setText("All field should be filled out.");
            showErrorTextTemporarily();
            return;
        } else if (!matchPasswords()) {
            return;
        }

        User user = new User(username, password);
        sendCreateAccountRequest(user);
    }

    @FXML
    protected void goBack() {
        if(SharedController.setNewScreen("login-screen")) {
            System.out.println("Back to login page.");
        }
    }

    private void showErrorTextTemporarily() {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }

    private boolean matchPasswords() {
        if (!passwordField.getText().equals(repeatPasswordField.getText())) {
            errorText.setText("Passwords don't match");
            showErrorTextTemporarily();

            passwordField.clear();
            repeatPasswordField.clear();
            return false;
        }
        return true;
    }

    private void sendCreateAccountRequest(User user) throws Exception {
        String publicKeyPath = "client-apk/src/main/resources/" + user.getUsername() + "Public.pubkey";
        String privateKeyPath = "client-apk/src/main/resources/" + user.getUsername() + "Private.privkey";
        CryptographicLibrary.generateAndStoreKeyPair("client-apk/src/main/resources/" + user.getUsername() + "Public.pubkey", "client-apk/src/main/resources/" + user.getUsername() +"Private.privkey");

        String url = "http://192.168.0.10:80/api/create-account";
        PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile(privateKeyPath);
        UserData userData = CryptographicLibrary.encryptUserDataJsonString(user.getUsername(), user.getPassword(), publicKeyPath, privateKey);
        String userDataJsonString = userData.convertUserDataToJsonString();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = SharedController.buildPostRequest(url, userDataJsonString);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> handleCreateAccountResponse(statusCode, user))
                .exceptionally(this::handleCreateAccountFailure);
    }

    private void handleCreateAccountResponse(int statusCode, User user) {
        if (statusCode == 200) {
            Platform.runLater(() -> {
                if(SharedController.setNewScreen("login-screen")) {
                    Main.currentUser = user.getUsername();
                    System.out.println("Account has been successfully created for Username: " + user.getUsername());
                }
            });
        } else {
            Platform.runLater(() -> {
                errorText.setText("Account creation failed.");
                showErrorTextTemporarily();
            });
        }
    }

    private Void handleCreateAccountFailure(Throwable e) {
        System.out.println("An error occurred during account creation: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("An error occurred.");
            showErrorTextTemporarily();
        });
        return null;
    }

}
