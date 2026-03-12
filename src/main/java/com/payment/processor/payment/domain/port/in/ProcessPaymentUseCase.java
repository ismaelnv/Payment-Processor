package com.payment.processor.payment.domain.port.in;

import com.payment.processor.payment.domain.model.Order;
import com.payment.processor.payment.domain.model.Transaction;
import reactor.core.publisher.Mono;

public interface ProcessPaymentUseCase {

    Mono<Transaction> process(Order order);
}
