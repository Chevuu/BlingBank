package sirs.com.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.util.Duration;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.CryptographicLibrary;
import main.java.sirs.com.crypto.CryptographicLibraryPackage.Data;
import sirs.com.Main;
import sirs.com.enums.TransactionStatus;
import sirs.com.models.Transaction;

import javafx.scene.control.TableView;

import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.*;

import static sirs.com.controller.SharedController.buildGetRequest;
import static sirs.com.controller.SharedController.setNewScreen;


public class TransactionListController implements Initializable {

    @FXML
    private TableView transactionTable;

    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;

    @FXML
    private TableColumn<Transaction, Double> valueColumn;

    @FXML
    private TableColumn<Transaction, String> descColumn;

    @FXML
    private TableColumn<Transaction, TransactionStatus> statusColumn;

    @FXML
    private Text errorText;

    @FXML
    protected void goBack() {
        if(setNewScreen("dashboard")) {
            System.out.println("User returned to dashboard.");
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("transactionDate"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        descColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("transactionStatus"));

        List<Transaction> allTransactions = fetchTransactions();

        transactionTable.getItems().addAll(allTransactions);
    }

    private List<Transaction> fetchTransactions() {
        System.out.println("fetching transactions");
        String username = Main.currentUser;
        String url = "http://192.168.0.10:80/api/transaction/getAll/" + username;
        //String url = "http://localhost:8080/api/transaction/getAll/" + username;
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = buildGetRequest(url);

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                String encryptedDataJsonString = response.body();
                List<String> transactionsJsonString = CryptographicLibrary.decryptTransactionsJsonString(encryptedDataJsonString);

               List<Transaction> transactionsList = new ArrayList<>();
                for (String transactionString : transactionsJsonString) {
                    transactionsList.add(Transaction.convertJsonToTransaction(transactionString));
                }
                return transactionsList;
            } else {
                errorText.setText("Failed to fetch transactions.");
                showErrorTextTemporarily();
                return Collections.emptyList();
            }
        } catch (Exception e) {
            System.out.println("An error occurred during transactions fetch: " + e.getMessage());
            Platform.runLater(() -> {
                errorText.setText("An error occurred.");
                showErrorTextTemporarily();
            });
            return Collections.emptyList();
        }
    }

    private void showErrorTextTemporarily() {
        errorText.setVisible(true);
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(event -> errorText.setVisible(false));
        pause.play();
    }
}