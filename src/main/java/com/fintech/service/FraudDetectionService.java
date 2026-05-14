package com.fintech.service;

import com.fintech.entity.FraudRule;
import com.fintech.exception.FraudDetectedException;
import com.fintech.repository.FraudRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class FraudDetectionService {

    @Autowired
    private FraudRuleRepository fraudRuleRepository;

    public void validate(BigDecimal amount) {

        BigDecimal limit = new BigDecimal("50000");

        if (amount.compareTo(limit) > 0) {
            throw new FraudDetectedException("Transaction blocked by fraud rule");
        }
    }
}