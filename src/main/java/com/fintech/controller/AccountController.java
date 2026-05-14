package com.fintech.controller;

import com.fintech.dto.request.CreateAccountRequest;
import com.fintech.dto.response.AccountResponse;
import com.fintech.dto.response.ApiResponse;
import com.fintech.service.AccountService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    @PostMapping
    public ApiResponse<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        return ApiResponse.ok("Account created successfully", accountService.createAccount(request));
    }

    @GetMapping("/{accountNumber}")
    public ApiResponse<AccountResponse> getAccountByAccountNumber(@PathVariable String accountNumber) {
        return ApiResponse.ok("Account fetched successfully", accountService.getAccountByAccountNumber(accountNumber));
    }
}