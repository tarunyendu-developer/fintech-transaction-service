package com.fintech.scheduler;

import com.fintech.entity.Transaction;
import com.fintech.enums.TransactionStatus;
import com.fintech.repository.TransactionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class ReconciliationScheduler {

    @Autowired
    private TransactionRepository transactionRepository;

    @Scheduled(fixedRate = 60000)
    public void reconcileTransactions() {

        log.info("Starting reconciliation process...");

        List<Transaction> processingTransactions = transactionRepository.findByStatus(TransactionStatus.PROCESSING);

        for (Transaction transaction :
                processingTransactions) {

            log.warn(
                    "Transaction stuck in PROCESSING state: {}",
                    transaction.getTransactionId()
            );
        }

        log.info("Reconciliation process completed.");
    }
}