package sirs.com.server.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import sirs.com.server.enums.Currency;

import java.util.List;

@Document(collection="accounts")
public class Account {
    private String id;
    private List<String> accountHolders;
    private double balance;
    private Currency currency;
    private List<Transaction> transactions;

    public Account() {}

    public Account(List<String> accountHolders, double balance, Currency currency, List<Transaction> transactions) {
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

    public static Account convertJsonToAccount(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, Account.class);
    }
}
