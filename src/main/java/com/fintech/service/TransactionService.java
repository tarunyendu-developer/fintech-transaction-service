package com.fintech.service;

import com.fintech.dto.request.CreditRequest;
import com.fintech.dto.request.DebitRequest;
import com.fintech.dto.request.ReverseTransactionRequest;
import com.fintech.dto.request.TransferRequest;
import com.fintech.dto.response.TransactionMetricsResponse;
import com.fintech.dto.response.TransactionResponse;
import com.fintech.entity.Account;
import com.fintech.entity.Transaction;
import com.fintech.enums.EventType;
import com.fintech.enums.TransactionStatus;
import com.fintech.enums.TransactionType;
import com.fintech.event.TransactionEvent;
import com.fintech.exception.AccountNotFoundException;
import com.fintech.exception.InsufficientBalanceException;
import com.fintech.repository.AccountRepository;
import com.fintech.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final FraudDetectionService fraudDetectionService;
    private final AuditService auditService;


    public TransactionService(
            TransactionRepository transactionRepository,
            AccountRepository accountRepository,
            FraudDetectionService fraudDetectionService,
            AuditService auditService
    ) {

        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.fraudDetectionService = fraudDetectionService;
        this.auditService = auditService;
    }

    // Transfer funds between accounts
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public TransactionResponse transfer(TransferRequest request) {

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            return TransactionResponse.from(existingTransaction.get());
        }

        Account fromAccount =
                accountRepository.findByAccountNumber(request.getFromAccountNumber())
                        .orElseThrow(() -> new AccountNotFoundException("Sender account not found"));

        Account toAccount =
                accountRepository.findByAccountNumber(request.getToAccountNumber())
                        .orElseThrow(() -> new AccountNotFoundException("Receiver account not found"));

        Transaction transaction =
                createTransaction(
                        fromAccount,
                        toAccount,
                        request.getAmount(),
                        TransactionType.TRANSFER,
                        TransactionStatus.INITIATED,
                        request.getIdempotencyKey()
                );

        transaction = transactionRepository.save(transaction);

        auditService.log(transaction, EventType.INITIATED, "Transfer initiated");

        fraudDetectionService.validate(request.getAmount());

        auditService.log(transaction, EventType.FRAUD_CHECK_PASSED, "Fraud validation passed");

        if (fromAccount.getBalance().compareTo(request.getAmount()) < 0) {

            transaction.setStatus(TransactionStatus.FAILED);
            transactionRepository.save(transaction);
            auditService.log(transaction, EventType.FAILED, "Insufficient balance");

            throw new InsufficientBalanceException("Insufficient balance");
        }

        transaction.setStatus(TransactionStatus.PROCESSING);

        transactionRepository.save(transaction);

        auditService.log(transaction, EventType.PROCESSING, "Updating balances");

        fromAccount.setBalance(fromAccount.getBalance().subtract(request.getAmount()));
        toAccount.setBalance(toAccount.getBalance().add(request.getAmount()));

        accountRepository.save(fromAccount);
        accountRepository.save(toAccount);

        transaction.setStatus(TransactionStatus.SUCCESS);

        transactionRepository.save(transaction);

        auditService.log(transaction, EventType.SUCCESS, "Transfer completed successfully");

        return TransactionResponse.from(transaction);
    }

    // Fetch all transactions
    public Page<TransactionResponse> getAllTransactions(int page, int size, String sortBy) {

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy).descending());
        Page<Transaction> transactions = transactionRepository.findAll(pageable);

        return transactions.map(TransactionResponse::from);
    }

    // Fetch transactions by account number
    public List<TransactionResponse> getTransactionsByAccountNumber(String accountNumber) {

        List<Transaction> transactions =
                transactionRepository
                        .findByFromAccount_AccountNumberOrToAccount_AccountNumber(accountNumber, accountNumber);

        return transactions.stream().map(TransactionResponse::from).toList();
    }

    // Debit operation
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public TransactionResponse debit(DebitRequest request) {

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            return TransactionResponse.from(existingTransaction.get());
        }

        Account account =
                accountRepository.findByAccountNumber(request.getAccountNumber())
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        fraudDetectionService.validate(request.getAmount());

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction =
                createTransaction(
                        null,
                        account,
                        request.getAmount(),
                        TransactionType.DEBIT,
                        TransactionStatus.SUCCESS,
                        request.getIdempotencyKey()
                );

        transaction = transactionRepository.save(transaction);

        auditService.log(transaction, EventType.SUCCESS, "Debit successful");

        return TransactionResponse.from(transaction);
    }

    // Credit operation
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public TransactionResponse credit(CreditRequest request) {

        Optional<Transaction> existingTransaction =
                transactionRepository.findByIdempotencyKey(request.getIdempotencyKey());

        if (existingTransaction.isPresent()) {
            return TransactionResponse.from(existingTransaction.get());
        }

        Account account =
                accountRepository.findByAccountNumber(request.getAccountNumber())
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        fraudDetectionService.validate(request.getAmount());

        account.setBalance(account.getBalance().add(request.getAmount()));

        accountRepository.save(account);

        Transaction transaction =
                createTransaction(
                        null,
                        account,
                        request.getAmount(),
                        TransactionType.CREDIT,
                        TransactionStatus.SUCCESS,
                        request.getIdempotencyKey()
                );

        transaction = transactionRepository.save(transaction);

        auditService.log(transaction, EventType.SUCCESS, "Credit successful");

        return TransactionResponse.from(transaction);
    }

    // Metrics API
    public TransactionMetricsResponse getMetrics() {

        TransactionMetricsResponse response = new TransactionMetricsResponse();
        response.setTotalTransactions(transactionRepository.count());
        response.setSuccessfulTransactions(transactionRepository.countByStatus(TransactionStatus.SUCCESS));
        response.setFailedTransactions(transactionRepository.countByStatus(TransactionStatus.FAILED));
        response.setDebitTransactions(transactionRepository.countByType(TransactionType.DEBIT));
        response.setCreditTransactions(transactionRepository.countByType(TransactionType.CREDIT));
        response.setTransferTransactions(transactionRepository.countByType(TransactionType.TRANSFER));

        return response;
    }

    // Get transaction by ID
    public TransactionResponse getTransactionById(String transactionId) {

        Transaction transaction =
                transactionRepository.findByTransactionId(transactionId)
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

        return TransactionResponse.from(transaction);
    }

    // Filter transactions
    public Page<TransactionResponse> getFilteredTransactions(
                             TransactionType type,
                             TransactionStatus status,
                             int page,
                             int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Page<Transaction> transactions =
                transactionRepository.findByTypeAndStatus(type, status, pageable);

        return transactions.map(TransactionResponse::from);
    }

    // Reverse transaction
    @Transactional
    @Retryable(retryFor = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    public TransactionResponse reverseTransaction(ReverseTransactionRequest request) {

        Transaction originalTransaction =
                transactionRepository.findByTransactionId(request.getTransactionId())
                        .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (originalTransaction.getStatus() == TransactionStatus.REVERSED) {
            throw new RuntimeException("Transaction already reversed");
        }

        if (originalTransaction.getType() != TransactionType.TRANSFER) {
            throw new RuntimeException("Only transfer transactions can be reversed");
        }

        Account sender = originalTransaction.getFromAccount();
        Account receiver =originalTransaction.getToAccount();
        BigDecimal amount = originalTransaction.getAmount();

        receiver.setBalance(receiver.getBalance().subtract(amount));
        sender.setBalance(sender.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);

        originalTransaction.setStatus(TransactionStatus.REVERSED);

        transactionRepository.save(originalTransaction);

        auditService.log(originalTransaction, EventType.REVERSED, "Transaction reversed successfully");

        return TransactionResponse.from(originalTransaction);
    }

    // Helper method to create a transaction
    private Transaction createTransaction(
            Account fromAccount,
            Account toAccount,
            BigDecimal amount,
            TransactionType type,
            TransactionStatus status,
            String idempotencyKey
    ) {

        Transaction transaction = new Transaction();
        transaction.setFromAccount(fromAccount);
        transaction.setToAccount(toAccount);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(status);
        transaction.setIdempotencyKey(idempotencyKey);

        return transactionRepository.save(transaction);
    }
}