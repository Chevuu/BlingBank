package sirs.com.controller;

import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.util.Duration;
import sirs.com.Main;
import sirs.com.models.User;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static sirs.com.controller.SharedController.*;

public class DashboardController {

    @FXML
    private Text errorText;

    @FXML
    protected void createBankAccount() {
        // String url = "http://192.168.0.10:80/api/hasBankAccount/" + Main.currentUser;
        String url = "http://localhost:8080/api/hasBankAccount/" + Main.currentUser;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildGetRequest(url);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> handleCreateBankAccountResponse(responseBody, Main.currentUser))
                .exceptionally(this::handleCreateBankAccountCheckFailure);
    }

    @FXML
    protected void listTransactions() {
        if(SharedController.setNewScreen("transaction-list")) {
            System.out.println("User: " + Main.currentUser + " has moved to listing transactions.");
        }
    }

    @FXML
    protected void showBankAccount() {
        String url = "http://192.168.0.10:80/api/hasBankAccount/" + Main.currentUser;
        //String url = "http://localhost:8080/api/hasBankAccount/" + Main.currentUser;

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildGetRequest(url);

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(responseBody -> handleBankAccountResponse(responseBody, Main.currentUser))
                .exceptionally(this::handleBankAccountCheckFailure);
    }

    @FXML
    protected void logOut() {
        String username = Main.currentUser;
        try {
            Main.setRoot("/screens/login-screen.fxml");
            System.out.println("User: " + username + " has logged out.");
            Main.currentUser = "-1";
        } catch (IOException e) {
            System.out.println("Wrong FXML path.");
        }
    }

    private void handleBankAccountResponse(String responseBody, String username) {
        boolean hasBankAccount = Boolean.parseBoolean(responseBody);
        Platform.runLater(() -> {
            if (hasBankAccount) {
                System.out.println("User " + username + " has a bank account.");
                if(SharedController.setNewScreen("show-bank-account")) {
                    System.out.println("User: " + Main.currentUser + " has moved to viewing their account.");
                }
            } else {
                System.out.println("User " + username + " does not have a bank account.");
                errorText.setText("Create an account first.");
                showErrorTextTemporarily();
            }
        });
    }

    private Void handleBankAccountCheckFailure(Throwable e) {
        System.out.println("An error occurred during bank account check: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("Error checking bank account.");
            showErrorTextTemporarily();
        });
        return null;
    }

    private void handleCreateBankAccountResponse(String responseBody, String username) {
        boolean hasBankAccount = Boolean.parseBoolean(responseBody);
        Platform.runLater(() -> {
            if (hasBankAccount) {
                System.out.println("User " + username + " has a bank account.");
                errorText.setText("You already have an account.");
                showErrorTextTemporarily();
            } else {
                System.out.println("User " + username + " does not have a bank account.");
                if(SharedController.setNewScreen("create-bank-account")) {
                    System.out.println("User: " + Main.currentUser + " has moved to account creation.");
                }
            }
        });
    }

    private Void handleCreateBankAccountCheckFailure(Throwable e) {
        System.out.println("An error occurred during bank account check: " + e.getMessage());
        Platform.runLater(() -> {
            errorText.setText("Error checking bank account.");
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
}
