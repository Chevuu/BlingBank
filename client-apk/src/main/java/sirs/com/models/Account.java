package sirs.com.models;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sirs.com.enums.Currency;

import java.util.ArrayList;
import java.util.List;

public class Account {
    public String[] accountHolders;
    public double balance;
    public Currency currency;
    public Transaction[] transactions;

    public Account(String[] accountHolders, double balance, Currency currency, Transaction[] transactions) {
        this.accountHolders = accountHolders;
        this.balance = balance;
        this.currency = currency;
        this.transactions = transactions;
    }

    public Account(String[] accountHolders, double balance, Currency currency) {
        this.accountHolders = accountHolders;
        this.balance = balance;
        this.currency = currency;
        this.transactions = new Transaction[4];
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
