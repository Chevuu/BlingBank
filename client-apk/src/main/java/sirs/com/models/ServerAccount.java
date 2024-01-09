package sirs.com.models;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import sirs.com.enums.Currency;

import java.util.List;

public class ServerAccount {
    private String id;
    private List<String> accountHolders;
    private double balance;
    private Currency currency;
    private List<Transaction> transactions;

    public ServerAccount() {}

    public ServerAccount(List<String> accountHolders, double balance, Currency currency, List<Transaction> transactions) {
        this.id = new ObjectId().toHexString();
        this.accountHolders = accountHolders;
        this.balance = balance;
        this.currency = currency;
        this.transactions = transactions;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getAccountHolders() {
        return accountHolders;
    }

    public void setAccountHolders(List<String> accountHolders) {
        this.accountHolders = accountHolders;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", balance=" + balance +
                ", currency=" + currency +
                ", transactions=" + transactions +
                '}';
    }

    public String convertAccountToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static ServerAccount convertJsonToAccount(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, ServerAccount.class);
    }
}
