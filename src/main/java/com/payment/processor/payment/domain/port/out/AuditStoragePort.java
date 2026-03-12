package com.payment.processor.payment.domain.port.out;

import com.payment.processor.payment.domain.model.Transaction;
import reactor.core.publisher.Mono;

public interface AuditStoragePort {

    Mono<Void> saveAudit(Transaction transaction);
}
