package sirs.com.server.model;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.mongodb.core.mapping.Document;
import sirs.com.server.enums.TransactionStatus;

@Document(collection="transactions")
public class ClientTransaction {

    public String username;
    public String transactionDate;
    public TransactionStatus transactionStatus;
    public double value;
    public String description;

    public ClientTransaction() {}

    public ClientTransaction(String username, String transactionDate, double value, String description) {
        this.username = username;
        this.transactionDate = transactionDate;
        this.transactionStatus = TransactionStatus.CREATED;
        this.value = value;
        this.description = description;
    }

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

    public static ClientTransaction convertJsonToTransaction(String jsonString) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(jsonString, ClientTransaction.class);
    }
}
