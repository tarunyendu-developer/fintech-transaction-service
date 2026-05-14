package com.fintech.service;

import com.fintech.entity.Transaction;
import com.fintech.entity.TransactionAudit;
import com.fintech.enums.EventType;
import com.fintech.repository.TransactionAuditRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    @Autowired
    private TransactionAuditRepository auditRepository;

    public void log(Transaction transaction, EventType eventType, String message) {

        TransactionAudit audit = new TransactionAudit();

        audit.setTransaction(transaction);
        audit.setEventType(eventType);
        audit.setMessage(message);

        auditRepository.save(audit);
    }
}