package com.fintech.dto.response;

import com.fintech.entity.Account;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AccountResponse {

    private String accountId;
    private String accountHolderName;
    private String accountNumber;
    private BigDecimal balance;
    private String status;

    public static AccountResponse from(Account account) {

        AccountResponse response = new AccountResponse();

        response.setAccountId(account.getAccountId());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setAccountNumber(account.getAccountNumber());
        response.setBalance(account.getBalance());
        response.setStatus(account.getStatus().name());

        return response;
    }

}