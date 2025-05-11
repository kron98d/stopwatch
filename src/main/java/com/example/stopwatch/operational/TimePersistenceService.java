package com.example.stopwatch.operational;

import com.example.stopwatch.domain.TimeEntry;
import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimePersistenceService {

    private final TimeEntryRepository repository;
    private final BlockingQueue<TimeEntry> buffer = new LinkedBlockingQueue<>();

    private ScheduledExecutorService producerScheduler;
    private ExecutorService consumerExecutor;

    @Value("${stopwatch.producer.interval-ms:1000}")
    private long producerIntervalMs;

    @Value("${stopwatch.shutdown.timeout-seconds:5}")
    private long shutdownTimeoutSec;

    @PostConstruct
    public void start() {
        log.info("Starting TimePersistenceService");

        this.producerScheduler = Executors.newSingleThreadScheduledExecutor(
                r -> new Thread(r, "time-producer-thread"));
        this.consumerExecutor = Executors.newSingleThreadExecutor(
                r -> new Thread(r, "time-consumer-thread"));

        producerScheduler.scheduleAtFixedRate(() -> {
            TimeEntry entry = new TimeEntry(Instant.now());
            buffer.offer(entry);
            log.debug("Produced: {}", entry.getTimestamp());
        }, 0, producerIntervalMs, TimeUnit.MILLISECONDS);

        consumerExecutor.submit(this::consumeLoop);
    }

    private void consumeLoop() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                TimeEntry entry = buffer.take();
                repository.save(entry);
                log.debug("Persisted: {}", entry);
            } catch (Exception e) {
                log.error("Failed to write to DB, will retry after delay", e);
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down TimePersistenceService");

        producerScheduler.shutdown();
        consumerExecutor.shutdown();
        try {
            if (!producerScheduler.awaitTermination(shutdownTimeoutSec, TimeUnit.SECONDS)) {
                log.warn("Producer did not terminate in time, forcing shutdown");
                producerScheduler.shutdownNow();
            }
            if (!consumerExecutor.awaitTermination(shutdownTimeoutSec, TimeUnit.SECONDS)) {
                log.warn("Consumer did not terminate in time, forcing shutdown");
                consumerExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Shutdown interrupted");
        }
    }
}