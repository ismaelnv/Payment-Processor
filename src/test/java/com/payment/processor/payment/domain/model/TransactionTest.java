package com.payment.processor.payment.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransactionTest {

    @Test
    void shouldCreateTransactionFromOrder() {
        Order order = Order.builder()
                .orderId("ORD-1001")
                .customerId("CUS-2001")
                .items(List.of(
                        OrderItem.builder().productId("PROD-001").quantity(2).build()
                ))
                .totalAmount(150.00)
                .createdAt(LocalDateTime.now())
                .build();

        Transaction transaction = Transaction.fromOrder(order);

        assertThat(transaction.getId()).isEqualTo("ORD-1001");
        assertThat(transaction.getCustomerId()).isEqualTo("CUS-2001");
        assertThat(transaction.getItems()).hasSize(1);
        assertThat(transaction.getTotalAmount()).isEqualTo(150.00);
        assertThat(transaction.getProcessedAt()).isNotNull();
    }
}
