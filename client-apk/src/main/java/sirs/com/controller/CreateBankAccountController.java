package sirs.com.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import sirs.com.Main;
import sirs.com.enums.Currency;
import sirs.com.models.Account;
import sirs.com.models.Transaction;
import sirs.com.models.User;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static sirs.com.controller.SharedController.buildGetRequest;
import static sirs.com.controller.SharedController.buildPostRequest;

public class CreateBankAccountController {

    @FXML
    TextField accountHoldersField;

    @FXML
    TextField currencyField;

    @FXML
    Text alertText;

    @FXML
    Text errorText;

    @FXML
    protected void createBankAccount() {
        String currentUser = Main.currentUser; // Cast to String if needed

        String[] otherHolders = accountHoldersField.getText().split(", ");
        String[] allHolders;
        if(otherHolders[0].isEmpty()) {
            allHolders = new String[] {currentUser};
        }
        else {
            allHolders = new String[otherHolders.length + 1];
            for (int i = 0; i < otherHolders.length; i++) {
                allHolders[i] = otherHolders[i];
            }
            allHolders[otherHolders.length] = currentUser;
        }
        int balance = 0;
        Currency currency;
        try {
            currency = Currency.valueOf(currencyField.getText());
        } catch (Exception e) {
            errorText.setText("Only GBP. USD and EUR are supported.");
            showErrorTextTemporarily(2);
            return;
        }

        Account account = new Account(allHolders, balance, currency, new Transaction[4]);
        sendCreateAccountRequest(account, currentUser);
        // System.out.println("user: " + Main.currentUser + " has created an account successfully");
    }

    @FXML
    protected void goBack() {
        if(SharedController.setNewScreen("dashboard")) {
            System.out.println("User returned to dashboard.");
        }
    }

    private void showAlertTextTemporarily() {
        alertText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> alertText.setVisible(false));
        pause.play();
    }

    private void showErrorTextTemporarily(int n) {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(n));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }

    private void sendCreateAccountRequest(Account account, String username) {
        String accountJsonString;
        String url = "http://192.168.0.10:80/api/account/create";
        //String url = "http://localhost:8080/api/account/create";
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request;

        try {
            accountJsonString = account.convertAccountToJsonString();
            //TODO load session key not symmetricKey
            Key sessionKey = CryptographicLibrary.readSecretKey("client-apk/src/main/resources/sessionKey.key");
            // encrypt account json string
            List<String> results = CryptographicLibrary.encryptJsonString(accountJsonString, sessionKey);
            String encryptedAccountJsonString = results.get(0);
            String iv = results.get(1);
            PublicKey publicKey = CryptographicLibrary.readPublicKeyFromFile("client-apk/src/main/resources/publicKeyServer.pubkey");
            // encrypt username
            String encryptedUsername = CryptographicLibrary.encryptUserWithPublicKey(username, publicKey);

            //add all to the data that is transmitted
            Data data = new Data(encryptedAccountJsonString, iv, encryptedUsername);
            request = buildPostRequest(url, data.convertDataToJsonString());
        } catch (Exception e) {
            System.out.println("There was a problem parsing or encrypting json");
            return;
        }

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(this::handleCreateAccountResponse)
                .exceptionally(this::handleCreateAccountFailure);
    }

    private void handleCreateAccountResponse(int statusCode) {
        if (statusCode == 200 || statusCode == 201) { // Assuming 200 OK or 201 Created status codes
            Platform.runLater(() -> {
                alertText.setText("Account successfully created.");
                showAlertTextTemporarily();
            });
        } else {
            Platform.runLater(() -> {
                errorText.setText("Failed to create account.");
                showErrorTextTemporarily(2);
            });
        }
    }

    private Void handleCreateAccountFailure(Throwable e) {
        System.out.println("An error occurred during account creation: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("An error occurred.");
            showErrorTextTemporarily(2);
        });
        return null;
    }
}
