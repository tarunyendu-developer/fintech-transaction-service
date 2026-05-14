package com.fintech.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class TransactionEvent {

    private String transactionId;

    private String transactionType;

    private BigDecimal amount;

    private String status;
}