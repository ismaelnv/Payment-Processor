package com.payment.processor.payment.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

    private String id;
    private String customerId;
    private List<OrderItem> items;
    private double totalAmount;
    private LocalDateTime processedAt;

    public static Transaction fromOrder(Order order) {
        return Transaction.builder()
                .id(order.getOrderId())
                .customerId(order.getCustomerId())
                .items(order.getItems())
                .totalAmount(order.getTotalAmount())
                .processedAt(LocalDateTime.now())
                .build();
    }
}
