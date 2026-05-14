package com.fintech.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReverseTransactionRequest {

    @NotBlank
    private String transactionId;
}