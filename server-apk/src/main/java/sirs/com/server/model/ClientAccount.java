package sirs.com.server.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sirs.com.server.enums.Currency;


import java.util.ArrayList;
import java.util.List;

public class ClientAccount {
    public String[] accountHolders;
    public double balance;
    public Currency currency;
    public Transaction[] transactions;

    public ClientAccount(String[] accountHolders, double balance, Currency currency, Transaction[] transactions) {
        this.accountHolders = accountHolders;
        this.balance = balance;
        this.currency = currency;
        this.transactions = transactions;
    }

    public ClientAccount(String[] accountHolders, double balance, Currency currency) {
        this.accountHolders = accountHolders;
        this.balance = balance;
        this.currency = currency;
        this.transactions = new Transaction[4];
    }

    public ClientAccount() {}

    public String convertAccountToJsonString() throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writeValueAsString(this);
    }

    public static ClientAccount convertJsonToAccount(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, ClientAccount.class);
    }
}
