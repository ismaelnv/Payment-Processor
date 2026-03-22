package com.payment.processor.payment.infrastructure.adapter.in.queue;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.processor.payment.domain.model.Order;
import com.payment.processor.payment.domain.port.in.ProcessPaymentUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class AzureQueueListener {

    private final QueueClient queueClient;
    private final ProcessPaymentUseCase processPaymentUseCase;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${azure.queue.poll-interval-ms}")
    public void pollMessages() {
        Flux.fromIterable(queueClient.receiveMessages(10))
                .flatMap(this::handleMessage)
                .subscribe();
    }

    private Mono<Void> handleMessage(QueueMessageItem message) {
        return decodeMessage(message.getBody().toString())
                .doOnNext(body -> log.info("Received message from queue: {}", body))
                .flatMap(body -> Mono.fromCallable(() -> objectMapper.readValue(body, Order.class))
                        .subscribeOn(Schedulers.boundedElastic())
                )
                .flatMap(order ->
                        processPaymentUseCase.process(order)
                                .then(Mono.fromCallable(() -> {
                                    queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
                                    return true;
                                }).subscribeOn(Schedulers.boundedElastic()))
                                .doOnSuccess(v -> log.info("Message deleted from queue for order: {}", order.getOrderId()))
                )
                .doOnError(e -> log.error("Error processing queue message: {}", e.getMessage(), e))
                .onErrorResume(e -> Mono.empty())
                .then();
    }

    private Mono<String> decodeMessage(String message) {
        return Mono.fromCallable(() -> new String(Base64.getDecoder().decode(message)))
                .onErrorReturn(IllegalArgumentException.class, message);
    }
}
