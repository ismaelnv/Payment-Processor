package com.payment.processor.payment.infrastructure.adapter.out.blob;

import com.azure.storage.blob.BlobContainerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.payment.processor.payment.domain.model.Transaction;
import com.payment.processor.payment.domain.port.out.AuditStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class AzureBlobAuditAdapter implements AuditStoragePort {

    private final BlobContainerClient blobContainerClient;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> saveAudit(Transaction transaction) {
        return Mono.<Void>fromRunnable(() -> {
            try {
                String json = objectMapper.writeValueAsString(transaction);
                byte[] bytes = json.getBytes(StandardCharsets.UTF_8);

                String blobName = String.format("audit/%s/%s.json",
                        LocalDate.now(), transaction.getId());

                blobContainerClient.getBlobClient(blobName)
                        .upload(new ByteArrayInputStream(bytes), bytes.length, true);

                log.info("Audit saved to Blob Storage: {}", blobName);
            } catch (Exception e) {
                throw new RuntimeException("Failed to save audit to Blob Storage", e);
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(10))
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .doBeforeRetry(signal -> log.warn("Retrying Blob Storage save for order: {}, attempt: {}",
                        transaction.getId(), signal.totalRetries() + 1)));
    }
}
