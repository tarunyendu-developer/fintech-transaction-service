package com.fintech.service;

import com.fintech.dto.request.CreateAccountRequest;
import com.fintech.dto.response.AccountResponse;
import com.fintech.entity.Account;
import com.fintech.exception.AccountNotFoundException;
import com.fintech.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    @Autowired
    private AccountRepository accountRepository;

// Create a new account based on the request and return the account details in the response
    public AccountResponse createAccount(CreateAccountRequest request) {

        Account account = new Account();

        account.setAccountHolderName(request.getAccountHolderName());
        account.setBalance(request.getInitialDeposit());

        Account savedAccount = accountRepository.save(account);

        AccountResponse response = new AccountResponse();

        response.setAccountId(savedAccount.getAccountId());
        response.setAccountHolderName(savedAccount.getAccountHolderName());
        response.setAccountNumber(savedAccount.getAccountNumber());
        response.setBalance(savedAccount.getBalance());
        response.setStatus(savedAccount.getStatus().name());

        return response;
    }
    public AccountResponse getAccountById(String accountId) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        AccountResponse response = new AccountResponse();

        response.setAccountId(account.getAccountId());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());

        return response;
    }
}