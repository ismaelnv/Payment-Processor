package com.payment.processor.payment.application.usecase;

import com.payment.processor.payment.domain.model.Order;
import com.payment.processor.payment.domain.model.Transaction;
import com.payment.processor.payment.domain.port.in.ProcessPaymentUseCase;
import com.payment.processor.payment.domain.port.out.AuditStoragePort;
import com.payment.processor.payment.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessPaymentUseCaseImpl implements ProcessPaymentUseCase {

    private final TransactionRepositoryPort transactionRepository;
    private final AuditStoragePort auditStorage;

    @Override
    public Mono<Transaction> process(Order order) {
        log.info("Processing order: {}", order.getOrderId());

        return Mono.just(Transaction.fromOrder(order))
                .flatMap(transactionRepository::save)
                .flatMap(saved -> auditStorage.saveAudit(saved).thenReturn(saved))
                .doOnSuccess(t -> log.info("Order {} processed successfully", t.getId()))
                .doOnError(error -> log.error("Failed to process order {}: {}",
                        order.getOrderId(), error.getMessage()));
    }
}
