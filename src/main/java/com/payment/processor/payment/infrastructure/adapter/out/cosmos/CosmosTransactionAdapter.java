package com.payment.processor.payment.infrastructure.adapter.out.cosmos;

import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.payment.processor.payment.domain.model.Transaction;
import com.payment.processor.payment.domain.port.out.TransactionRepositoryPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class CosmosTransactionAdapter implements TransactionRepositoryPort {

    private final CosmosContainer cosmosContainer;

    @Override
    public Mono<Transaction> save(Transaction transaction) {
        return Mono.fromCallable(() -> {
            cosmosContainer.upsertItem(transaction, new PartitionKey(transaction.getId()),
                    new CosmosItemRequestOptions());
            log.info("Transaction saved to Cosmos DB for order: {}", transaction.getId());
            return transaction;
        })
        .subscribeOn(Schedulers.boundedElastic())
        .timeout(Duration.ofSeconds(10))
        .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .doBeforeRetry(signal -> log.warn("Retrying Cosmos DB save for order: {}, attempt: {}",
                        transaction.getId(), signal.totalRetries() + 1)));
    }
}
