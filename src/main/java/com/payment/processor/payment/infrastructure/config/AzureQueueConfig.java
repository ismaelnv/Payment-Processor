package com.payment.processor.payment.infrastructure.config;

import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureQueueConfig {

    @Value("${azure.queue.connection-string}")
    private String connectionString;

    @Value("${azure.queue.queue-name}")
    private String queueName;

    @Bean
    public QueueClient queueClient() {
        QueueClient client = new QueueClientBuilder()
                .connectionString(connectionString)
                .queueName(queueName)
                .buildClient();
        client.createIfNotExists();
        return client;
    }
}
