package sirs.com.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import sirs.com.Main;
import sirs.com.enums.TransactionStatus;
import sirs.com.models.Transaction;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.Key;
import java.security.PrivateKey;
import java.time.Instant;
import java.util.List;

import static sirs.com.controller.SharedController.buildPostRequest;
import static sirs.com.controller.SharedController.setNewScreen;
public class RequestTransactionController {

    @FXML
    private TextField topOffField;

    @FXML
    private TextField withdrawField;

    @FXML
    private TextField descriptionField;

    @FXML
    private Text errorText;

    @FXML
    protected void goBack() {
        if(setNewScreen("dashboard")) {
            System.out.println("User returned to dashboard.");
        }
    }

    @FXML
    protected void requestTransaction() {
        try {
            double topOff = Double.parseDouble(topOffField.getText());
            double withdraw = Double.parseDouble(withdrawField.getText());
            String desc = descriptionField.getText();

            Transaction transaction = new Transaction(Main.currentUser, Instant.now().toString(), topOff - withdraw, desc);
            transaction.setTransactionStatus(TransactionStatus.CREATED);

            String privateKeyPath = "client-apk/src/main/resources/" + Main.currentUser + "Private.privkey";
            Key sessionKey = CryptographicLibrary.readSymmetricKey("client-apk/src/main/resources/sessionKey.key");
            String transactionJsonString = transaction.convertTransactionToJsonString();
            List<String> results = CryptographicLibrary.encryptJsonString(transactionJsonString, sessionKey);
            Data data = new Data(results.get(0), results.get(1), "");
            PrivateKey privateKey = CryptographicLibrary.readPrivateKeyFromFile(privateKeyPath);
            String digitalSignature = CryptographicLibrary.createSignature(privateKey, results.get(0));
            data.setDigitalSignature(digitalSignature);
            data.setTimestamp(Instant.now().toString());
            String dataJsonString = data.convertDataToJsonString();

            sendTransactionToServer(dataJsonString);
        } catch (NumberFormatException e) {
            errorText.setText("Please enter valid numbers for top off and withdraw.");
            showErrorTextTemporarily();
        } catch (JsonProcessingException e) {
            errorText.setText("Error processing transaction details.");
            showErrorTextTemporarily();
        } catch (Exception e) {
            errorText.setText("An unexpected error occurred.");
            showErrorTextTemporarily();
        }
    }

    private void sendTransactionToServer(String encryptedTransactionJson) throws Exception {
        String url = "http://192.168.0.10:80/api/transaction/add" + Main.currentUser;
        //String url = "http://localhost:8080/api/transaction/add/" + Main.currentUser;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildPostRequest(url, encryptedTransactionJson);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::statusCode)
                .thenAccept(statusCode -> {
                    if (statusCode == 200) {
                        Platform.runLater(() -> {
                            if(setNewScreen("dashboard")) {
                                System.out.println("User returned to dashboard.");
                            }
                        });
                        System.out.println("Transaction completed successfully.");
                    } else {
                        Platform.runLater(() -> {
                            errorText.setText("Failed to complete transaction.");
                            showErrorTextTemporarily();
                        });
                    }
                })
                .exceptionally(e -> {
                    System.out.println("Error sending transaction to server: " + e.getMessage());
                    Platform.runLater(() -> {
                        errorText.setText("An error occurred while sending the transaction.");
                        showErrorTextTemporarily();
                    });
                    return null;
                });
    }

    private void showErrorTextTemporarily() {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }
}
