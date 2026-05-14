package com.fintech.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "fraud_rule")
@Getter
@Setter
@NoArgsConstructor
public class FraudRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rule_id")
    private Integer ruleId;

    @Column(name = "rule_name", unique = true, nullable = false, length = 100)
    private String ruleName;

    @Column(name = "max_amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal maxAmountLimit;

    @Column(nullable = false)
    private Boolean enabled = true;
}