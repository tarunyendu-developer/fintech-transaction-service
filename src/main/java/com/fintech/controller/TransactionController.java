package com.fintech.controller;

import com.fintech.dto.request.CreditRequest;
import com.fintech.dto.request.DebitRequest;
import com.fintech.dto.request.ReverseTransactionRequest;
import com.fintech.dto.request.TransferRequest;
import com.fintech.dto.response.ApiResponse;
import com.fintech.dto.response.TransactionMetricsResponse;
import com.fintech.dto.response.TransactionResponse;
import com.fintech.enums.TransactionStatus;
import com.fintech.enums.TransactionType;
import com.fintech.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // Endpoint to transfer funds between accounts
    @PostMapping("/transfer")
    public ApiResponse<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ApiResponse.ok("Transfer successful", transactionService.transfer(request));
    }

    // Endpoint to fetch all transactions
    @GetMapping
    public ApiResponse<Page<TransactionResponse>> getAllTransactions(
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size,
            @RequestParam(defaultValue = "createdAt")
            String sortBy
    ) {

        return ApiResponse.ok("Transactions fetched successfully", transactionService.getAllTransactions(page, size, sortBy));
    }

    // Endpoint to fetch transactions by account number
    @GetMapping("/account/{accountNumber}")
    public ApiResponse<List<TransactionResponse>> getTransactionsByAccountNumber(@PathVariable String accountNumber) {
        return ApiResponse.ok("Transactions fetched successfully", transactionService.getTransactionsByAccountNumber(accountNumber));
    }

    // Additional endpoint to handle debit transactions
    @PostMapping("/debit")
    public ApiResponse<TransactionResponse> debit(@Valid @RequestBody DebitRequest request) {
        return ApiResponse.ok("Debit successful", transactionService.debit(request));
    }

    // Additional endpoint to handle credit transactions
    @PostMapping("/credit")
    public ApiResponse<TransactionResponse> credit(@Valid @RequestBody CreditRequest request) {
        return ApiResponse.ok("Credit successful", transactionService.credit(request));
    }

    // Endpoint to fetch transaction metrics
    @GetMapping("/metrics")
    public ApiResponse<TransactionMetricsResponse> getMetrics() {
        return ApiResponse.ok("Transaction metrics fetched successfully", transactionService.getMetrics());
    }

    // Endpoint to fetch transaction details by transaction ID
    @GetMapping("/id/{transactionId}")
    public ApiResponse<TransactionResponse> getTransactionById(@PathVariable String transactionId) {
        return ApiResponse.ok("Transaction fetched successfully", transactionService.getTransactionById(transactionId));
    }

    // Endpoint to fetch transactions filtered by type and status
    @GetMapping("/filter")
    public ApiResponse<Page<TransactionResponse>> getFilteredTransactions(
            @RequestParam TransactionType type,
            @RequestParam TransactionStatus status,
            @RequestParam(defaultValue = "0")
            int page,
            @RequestParam(defaultValue = "5")
            int size
    ) {

        return ApiResponse.ok("Filtered transactions fetched successfully",
                transactionService.getFilteredTransactions(type, status, page, size));
    }

    // Endpoint to reverse a transaction
    @PostMapping("/reverse")
    public ApiResponse<TransactionResponse> reverseTransaction(@Valid @RequestBody ReverseTransactionRequest request) {
        return ApiResponse.ok("Transaction reversed successfully", transactionService.reverseTransaction(request));
    }
}