package com.fintech.dto.response;

import com.fintech.entity.Transaction;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class TransactionResponse {

    private String transactionId;
    private String fromAccountNumber;
    private String toAccountNumber;
    private BigDecimal amount;
    private String transactionType;
    private String transactionStatus;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction transaction) {

        TransactionResponse response = new TransactionResponse();

        response.setTransactionId(transaction.getTransactionId());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(String.valueOf(transaction.getType()));
        response.setTransactionStatus(String.valueOf(transaction.getStatus()));

        if (transaction.getFromAccount() != null) {
            response.setFromAccountNumber(transaction.getFromAccount().getAccountNumber());
        }
        if (transaction.getToAccount() != null) {
            response.setToAccountNumber(transaction.getToAccount().getAccountNumber());
        }
        response.setCreatedAt(transaction.getCreatedAt());

        return response;
    }

}