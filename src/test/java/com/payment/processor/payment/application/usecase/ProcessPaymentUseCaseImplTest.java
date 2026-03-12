package com.payment.processor.payment.application.usecase;

import com.payment.processor.payment.domain.model.Order;
import com.payment.processor.payment.domain.model.OrderItem;
import com.payment.processor.payment.domain.model.Transaction;
import com.payment.processor.payment.domain.port.out.AuditStoragePort;
import com.payment.processor.payment.domain.port.out.TransactionRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessPaymentUseCaseImplTest {

    @Mock
    private TransactionRepositoryPort transactionRepository;

    @Mock
    private AuditStoragePort auditStorage;

    @InjectMocks
    private ProcessPaymentUseCaseImpl processPaymentUseCase;

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .orderId("ORD-1001")
                .customerId("CUS-2001")
                .items(List.of(
                        OrderItem.builder().productId("PROD-001").quantity(2).build(),
                        OrderItem.builder().productId("PROD-002").quantity(1).build()
                ))
                .totalAmount(160.50)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void shouldProcessOrderAndSaveTransactionAndAudit() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(auditStorage.saveAudit(any(Transaction.class)))
                .thenReturn(Mono.empty());

        StepVerifier.create(processPaymentUseCase.process(order))
                .assertNext(transaction -> {
                    assertThat(transaction.getId()).isEqualTo("ORD-1001");
                    assertThat(transaction.getCustomerId()).isEqualTo("CUS-2001");
                    assertThat(transaction.getTotalAmount()).isEqualTo(160.50);
                    assertThat(transaction.getItems()).hasSize(2);
                    assertThat(transaction.getProcessedAt()).isNotNull();
                })
                .verifyComplete();

        verify(transactionRepository).save(any(Transaction.class));
        verify(auditStorage).saveAudit(any(Transaction.class));
    }

    @Test
    void shouldLogErrorWhenRepositoryFails() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Cosmos DB connection failed")));

        StepVerifier.create(processPaymentUseCase.process(order))
                .expectError(RuntimeException.class)
                .verify();

        verify(transactionRepository).save(any(Transaction.class));
        verify(auditStorage, never()).saveAudit(any(Transaction.class));
    }

    @Test
    void shouldLogErrorWhenAuditFails() {
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(auditStorage.saveAudit(any(Transaction.class)))
                .thenReturn(Mono.error(new RuntimeException("Blob Storage connection failed")));

        StepVerifier.create(processPaymentUseCase.process(order))
                .expectError(RuntimeException.class)
                .verify();

        verify(transactionRepository).save(any(Transaction.class));
        verify(auditStorage).saveAudit(any(Transaction.class));
    }
}
