package com.fintech.service;

import com.fintech.dto.request.CreateAccountRequest;
import com.fintech.dto.response.AccountResponse;
import com.fintech.entity.Account;
import com.fintech.exception.AccountNotFoundException;
import com.fintech.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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

        return AccountResponse.from(savedAccount);
    }

    // Fetch account details by account ID and return the account details in the response
    public AccountResponse getAccountByAccountNumber(String accountNumber) {

        Account account = accountRepository.findByAccountNumber(accountNumber)
                        .orElseThrow(() -> new AccountNotFoundException("Account not found"));

        return AccountResponse.from(account);
    }


}