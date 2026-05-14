package com.fintech.repository;

import com.fintech.entity.Transaction;
import com.fintech.enums.TransactionStatus;
import com.fintech.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, String> {

    List<Transaction> findByFromAccount_AccountNumberOrToAccount_AccountNumber(String fromAccountNumber, String toAccountNumber);
    Optional<Transaction> findByIdempotencyKey(String idempotencyKey);
    Long countByStatus(TransactionStatus status);
    Long countByType(TransactionType type);
    Optional<Transaction> findByTransactionId(String transactionId);
    Page<Transaction> findByTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);
    List<Transaction> findByStatus(TransactionStatus status);


}