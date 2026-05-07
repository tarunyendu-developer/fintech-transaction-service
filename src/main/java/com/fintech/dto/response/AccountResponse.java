package com.fintech.dto.response;

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

}