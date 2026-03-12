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
        queueClient.receiveMessages(10).forEach(this::handleMessage);
    }

    private void handleMessage(QueueMessageItem message) {
        try {
            String body = decodeMessage(message.getBody().toString());
            log.info("Received message from queue: {}", body);

            Order order = objectMapper.readValue(body, Order.class);

            processPaymentUseCase.process(order).block();

            queueClient.deleteMessage(message.getMessageId(), message.getPopReceipt());
            log.info("Message deleted from queue for order: {}", order.getOrderId());
        } catch (Exception e) {
            log.error("Error processing queue message: {}", e.getMessage(), e);
        }
    }

    private String decodeMessage(String message) {
        try {
            return new String(Base64.getDecoder().decode(message));
        } catch (IllegalArgumentException e) {
            return message;
        }
    }
}
