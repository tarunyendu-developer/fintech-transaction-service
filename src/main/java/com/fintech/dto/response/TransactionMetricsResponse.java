package com.fintech.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionMetricsResponse {

    private Long totalTransactions;
    private Long successfulTransactions;
    private Long failedTransactions;
    private Long debitTransactions;
    private Long creditTransactions;
    private Long transferTransactions;
}