package com.payment.processor.payment.infrastructure.adapter.in.queue;

import com.azure.core.util.BinaryData;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.models.QueueMessageItem;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.payment.processor.payment.domain.model.Transaction;
import com.payment.processor.payment.domain.port.in.ProcessPaymentUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AzureQueueListenerTest {

    @Mock
    private QueueClient queueClient;

    @Mock
    private ProcessPaymentUseCase processPaymentUseCase;

    private AzureQueueListener azureQueueListener;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        azureQueueListener = new AzureQueueListener(queueClient, processPaymentUseCase, objectMapper);
    }

    @Test
    void shouldPollMessagesFromQueue() {
        when(queueClient.receiveMessages(10)).thenReturn(new com.azure.core.http.rest.PagedIterable<>(
                new com.azure.core.http.rest.PagedFlux<>(() -> reactor.core.publisher.Mono.empty())
        ));

        azureQueueListener.pollMessages();

        verify(queueClient).receiveMessages(10);
    }
}
