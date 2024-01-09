package sirs.com.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sirs.com.Main;
import sirs.com.models.Account;
import sirs.com.models.ServerAccount;
import sirs.com.models.Transaction;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.ResourceBundle;

import static sirs.com.controller.SharedController.*;

public class AccountDisplayController implements Initializable {

    @FXML
    private TextField holdersField;

    @FXML
    private TextField balanceField;

    @FXML
    private TextField currencyField;

    @FXML
    private Text errorText;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        fillAllFields();
    }

    public void fillAllFields() {
        holdersField.clear();
        balanceField.clear();
        currencyField.clear();

        String url = "http://192.168.0.10:80/api/account/get/" + Main.currentUser;
        //String url = "http://localhost:8080/api/account/get/" + Main.currentUser;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildGetRequest(url);
/*
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(this::updateFields)
                .exceptionally(e -> {
                    System.out.println("Error fetching bank account.");
                    return null;
                });

 */

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    int statusCode = response.statusCode(); // Retrieve the status code
                    String responseBody = response.body(); // Retrieve the response body as a string
                    return new Pair<>(statusCode, responseBody); // Return status code and response body as a pair
                })
                .thenAccept(pair -> updateFields(pair.getKey(), pair.getValue()))
                .exceptionally(this::handleAccountDisplayFailure);

    }

    private void updateFields(int statusCode, String encryptedDataJsonString) {
        if(statusCode == 200 || statusCode == 201) {
            try {
                PublicKey publicKey = CryptographicLibrary.readPublicKeyFromFile("client-apk/src/main/resources/publicKeyServer.pubkey");
                Key sessionKey = CryptographicLibrary.readSecretKey("client-apk/src/main/resources/sessionKey.key");
                String decryptedAccountJsonString = CryptographicLibrary.decryptDataJsonString(encryptedDataJsonString,publicKey, sessionKey);
                ServerAccount serverAccount = ServerAccount.convertJsonToAccount(decryptedAccountJsonString);
                Account account = new Account(serverAccount.getAccountHolders().toArray(new String[0]), serverAccount.getBalance(), serverAccount.getCurrency(), serverAccount.getTransactions().toArray(new Transaction[0]));

                holdersField.setText(Arrays.toString(account.accountHolders));
                balanceField.setText(Double.toString(account.balance));
                currencyField.setText(account.currency.toString());
            } catch (Exception e) {
                System.out.println("Error parsing bank account JSON.");
            }
        } else {
            Platform.runLater(() -> {
                errorText.setText("Error displaying bank account.");
                showErrorTextTemporarily();
            });
        }

    }

    private Void handleAccountDisplayFailure(Throwable e) {
        System.out.println("An error occurred during bank account sisplay: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("Error displaying bank account.");
            showErrorTextTemporarily();
        });
        return null;
    }

    @FXML
    protected void goBack() {
        if(setNewScreen("dashboard")) {
            System.out.println("User returned to dashboard.");
        }
    }

    @FXML
    protected void deleteAccount() {
        String url = "http://192.168.0.10:80/api/user/deleteAccount/" + Main.currentUser;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildDeleteRequest(url);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> {
                    if (statusCode == 200) {
                        System.out.println("Account successfully deleted.");
                        Platform.runLater(() -> {
                            if (setNewScreen("dashboard")) {
                                System.out.println("User returned to dashboard after account deletion.");
                            }
                        });
                    } else {
                        System.out.println("Failed to delete account. Status code: " + statusCode);
                    }
                })
                .exceptionally(e -> {
                    System.out.println("An error occurred during account deletion: " + e.getMessage());
                    return null;
                });
    }

    @FXML
    protected void requestTransaction() {
        if(setNewScreen("request-transaction")) {
            System.out.println("User moved to request a transaction.");
        }
    }

    private void showErrorTextTemporarily() {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }
}
