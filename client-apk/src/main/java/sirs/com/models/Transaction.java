package sirs.com.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sirs.com.enums.TransactionStatus;

import java.time.LocalDate;
import java.util.Date;

public class Transaction {

    private String username;
    private String transactionDate;
    private TransactionStatus transactionStatus;
    private double value;
    private String description;

    public Transaction (String username, String transactionDate, double value, String description) {
        this.username = username;
        this.transactionDate = transactionDate;
        this.transactionStatus = TransactionStatus.CREATED;
        this.value = value;
        this.description = description;
    }

    public Transaction() {}

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        this.transactionDate = transactionDate;
    }

    public TransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    public void setTransactionStatus(TransactionStatus transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionDate=" + transactionDate +
                ", transactionStatus=" + transactionStatus +
                ", value=" + value +
                ", description='" + description + '\'' +
                '}';
    }

    public String convertTransactionToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static Transaction convertJsonToTransaction(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Transaction.class);
    }
}
