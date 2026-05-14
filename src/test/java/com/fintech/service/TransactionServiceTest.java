package com.fintech.service;

import com.fintech.dto.request.CreditRequest;
import com.fintech.dto.request.DebitRequest;
import com.fintech.dto.request.TransferRequest;
import com.fintech.dto.response.TransactionResponse;
import com.fintech.entity.Account;
import com.fintech.entity.Transaction;
import com.fintech.enums.TransactionStatus;
import com.fintech.enums.TransactionType;
import com.fintech.exception.AccountNotFoundException;
import com.fintech.exception.FraudDetectedException;
import com.fintech.exception.InsufficientBalanceException;
import com.fintech.repository.AccountRepository;
import com.fintech.repository.TransactionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private FraudDetectionService fraudDetectionService;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TransactionService transactionService;

    @Test
    void transfer_Success() {

        Account sender = new Account();
        sender.setBalance(new BigDecimal("5000"));
        sender.setAccountNumber("ACC1001");

        Account receiver = new Account();
        receiver.setBalance(new BigDecimal("1000"));
        receiver.setAccountNumber("ACC2001");

        TransferRequest request = new TransferRequest();

        request.setFromAccountNumber("SENDER_ID");
        request.setToAccountNumber("RECEIVER_ID");
        request.setAmount(new BigDecimal("1000"));
        request.setIdempotencyKey("TXN-1001");

        when(accountRepository.findByAccountNumber("SENDER_ID"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumber("RECEIVER_ID"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository
                .findByIdempotencyKey("TXN-1001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        TransactionResponse response =
                transactionService.transfer(request);

        assertNotNull(response);

        assertEquals(
                new BigDecimal("4000"),
                sender.getBalance()
        );

        assertEquals(
                new BigDecimal("2000"),
                receiver.getBalance()
        );

        verify(accountRepository, times(2))
                .save(any(Account.class));
    }

    @Test
    void transfer_InsufficientBalance() {

        Account sender = new Account();
        sender.setBalance(new BigDecimal("500"));

        Account receiver = new Account();
        receiver.setBalance(new BigDecimal("1000"));

        TransferRequest request =
                new TransferRequest();

        request.setFromAccountNumber("SENDER_ID");
        request.setToAccountNumber("RECEIVER_ID");
        request.setAmount(new BigDecimal("1000"));
        request.setIdempotencyKey("TXN-2001");

        when(accountRepository.findByAccountNumber("SENDER_ID"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumber("RECEIVER_ID"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository
                .findByIdempotencyKey("TXN-2001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        InsufficientBalanceException exception =
                assertThrows(
                        InsufficientBalanceException.class,
                        () -> transactionService.transfer(request)
                );

        assertEquals(
                "Insufficient balance",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void transfer_FraudDetected() {

        Account sender = new Account();
        sender.setBalance(new BigDecimal("100000"));

        Account receiver = new Account();
        receiver.setBalance(new BigDecimal("5000"));

        TransferRequest request =
                new TransferRequest();

        request.setFromAccountNumber("SENDER_ID");
        request.setToAccountNumber("RECEIVER_ID");
        request.setAmount(new BigDecimal("70000"));
        request.setIdempotencyKey("TXN-3001");

        when(accountRepository.findByAccountNumber("SENDER_ID"))
                .thenReturn(Optional.of(sender));

        when(accountRepository.findByAccountNumber("RECEIVER_ID"))
                .thenReturn(Optional.of(receiver));

        when(transactionRepository
                .findByIdempotencyKey("TXN-3001"))
                .thenReturn(Optional.empty());

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        doThrow(
                new FraudDetectedException(
                        "Transaction blocked by fraud rule"
                )
        ).when(fraudDetectionService)
                .validate(new BigDecimal("70000"));

        FraudDetectedException exception =
                assertThrows(
                        FraudDetectedException.class,
                        () -> transactionService.transfer(request)
                );

        assertEquals(
                "Transaction blocked by fraud rule",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any(Account.class));
    }

    @Test
    void transfer_IdempotencyKeyAlreadyExists() {

        Account sender = new Account();
        sender.setAccountNumber("ACC1001");

        Account receiver = new Account();
        receiver.setAccountNumber("ACC2001");

        Transaction existingTransaction =
                new Transaction();

        existingTransaction.setFromAccount(sender);

        existingTransaction.setToAccount(receiver);

        existingTransaction.setStatus(
                TransactionStatus.SUCCESS
        );

        existingTransaction.setType(
                TransactionType.TRANSFER
        );

        existingTransaction.setAmount(
                new BigDecimal("1000")
        );

        TransferRequest request =
                new TransferRequest();

        request.setFromAccountNumber("SENDER_ID");
        request.setToAccountNumber("RECEIVER_ID");
        request.setAmount(new BigDecimal("1000"));
        request.setIdempotencyKey("TXN-4001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-4001"))
                .thenReturn(Optional.of(existingTransaction));

        TransactionResponse response =
                transactionService.transfer(request);

        assertNotNull(response);

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void transfer_SenderAccountNotFound() {

        TransferRequest request =
                new TransferRequest();

        request.setFromAccountNumber("INVALID_ID");
        request.setToAccountNumber("RECEIVER_ID");
        request.setAmount(new BigDecimal("1000"));
        request.setIdempotencyKey("TXN-5001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-5001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumber("INVALID_ID"))
                .thenReturn(Optional.empty());

        AccountNotFoundException exception =
                assertThrows(
                        AccountNotFoundException.class,
                        () -> transactionService.transfer(request)
                );

        assertEquals(
                "Sender account not found",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void credit_Success() {

        Account account = new Account();

        account.setBalance(
                new BigDecimal("1000")
        );

        account.setAccountNumber("ACC1001");

        CreditRequest request =
                new CreditRequest();

        request.setAccountNumber("ACCOUNT_ID");

        request.setAmount(
                new BigDecimal("500")
        );

        request.setIdempotencyKey("TXN-6001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-6001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumber("ACCOUNT_ID"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        TransactionResponse response =
                transactionService.credit(request);

        assertNotNull(response);

        assertEquals(
                new BigDecimal("1500"),
                account.getBalance()
        );

        verify(accountRepository, times(1))
                .save(any(Account.class));

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }

    @Test
    void debit_Success() {

        Account account = new Account();

        account.setBalance(
                new BigDecimal("5000")
        );

        account.setAccountNumber("ACC1001");

        DebitRequest request =
                new DebitRequest();

        request.setAccountNumber("ACCOUNT_ID");

        request.setAmount(
                new BigDecimal("1000")
        );

        request.setIdempotencyKey("TXN-7001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-7001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumber("ACCOUNT_ID"))
                .thenReturn(Optional.of(account));

        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );

        TransactionResponse response =
                transactionService.debit(request);

        assertNotNull(response);

        assertEquals(
                new BigDecimal("4000"),
                account.getBalance()
        );

        verify(accountRepository, times(1))
                .save(any(Account.class));

        verify(transactionRepository, times(1))
                .save(any(Transaction.class));
    }

    @Test
    void debit_InsufficientBalance() {

        Account account = new Account();

        account.setBalance(
                new BigDecimal("500")
        );

        account.setAccountNumber("ACC1001");

        DebitRequest request =
                new DebitRequest();

        request.setAccountNumber("ACCOUNT_ID");

        request.setAmount(
                new BigDecimal("1000")
        );

        request.setIdempotencyKey("TXN-8001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-8001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumber("ACCOUNT_ID"))
                .thenReturn(Optional.of(account));

        InsufficientBalanceException exception =
                assertThrows(
                        InsufficientBalanceException.class,
                        () -> transactionService.debit(request)
                );

        assertEquals(
                "Insufficient balance",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }

    @Test
    void credit_FraudDetected() {

        Account account = new Account();

        account.setBalance(
                new BigDecimal("5000")
        );

        account.setAccountNumber("ACC1001");

        CreditRequest request =
                new CreditRequest();

        request.setAccountNumber("ACCOUNT_ID");

        request.setAmount(
                new BigDecimal("100000")
        );

        request.setIdempotencyKey("TXN-9001");

        when(transactionRepository
                .findByIdempotencyKey("TXN-9001"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumber("ACCOUNT_ID"))
                .thenReturn(Optional.of(account));

        doThrow(
                new FraudDetectedException(
                        "Transaction blocked by fraud rule"
                )
        ).when(fraudDetectionService)
                .validate(new BigDecimal("100000"));

        FraudDetectedException exception =
                assertThrows(
                        FraudDetectedException.class,
                        () -> transactionService.credit(request)
                );

        assertEquals(
                "Transaction blocked by fraud rule",
                exception.getMessage()
        );

        verify(accountRepository, never())
                .save(any(Account.class));

        verify(transactionRepository, never())
                .save(any(Transaction.class));
    }
}