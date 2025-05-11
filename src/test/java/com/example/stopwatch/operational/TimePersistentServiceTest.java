package com.example.stopwatch.operational;

import com.example.stopwatch.AbstractIntegrationTest;
import com.example.stopwatch.domain.TimeEntry;
import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import eu.rekawek.toxiproxy.model.ToxicDirection;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

public class TimePersistentServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TimeEntryRepository repository;

    @Test
    @SneakyThrows
    @DisplayName("Проверка: временные записи сохраняются и упорядочены по возрастанию")
    void testTimestampsArePersistedAndOrdered() {
        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<TimeEntry> all = repository.findAll();
            assertThat(all.size()).isGreaterThanOrEqualTo(3);

            List<TimeEntry> sorted = all.stream()
                    .sorted(Comparator.comparing(TimeEntry::getTimestamp))
                    .toList();

            assertThat(all).containsExactlyElementsOf(sorted);
        });
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка восстановления после временного сбоя соединения с БД (через Toxiproxy)")
    void shouldRecoverFromConnectionDrop() {
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> assertThat(repository.findAll()).isNotEmpty());

        proxy.setConnectionCut(true);
        TimeUnit.SECONDS.sleep(3);

        proxy.setConnectionCut(false);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(repository.findAll()).isNotEmpty());
    }

    @Test
    @SneakyThrows
    @DisplayName("Буфер наполняется при временном недоступном соединении (через Toxiproxy)")
    void shouldBufferEntriesWhenConnectionIsCut() {
        proxy.setConnectionCut(true);
        TimeUnit.SECONDS.sleep(3);

        proxy.setConnectionCut(false);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> assertThat(repository.findAll().size()).isGreaterThan(0));
    }

    @Test
    @SneakyThrows
    @DisplayName("Producer генерирует записи в нужном интервале")
    void shouldProduceEntriesEverySecond() {
        await().atMost(6, TimeUnit.SECONDS).untilAsserted(() -> assertThat(repository.findAll().size()).isGreaterThanOrEqualTo(4));
    }

    @Test
    @SneakyThrows
    @DisplayName("Producer стабильно генерирует много записей без ошибок")
    void shouldHandleLargeVolumeWithoutError() {
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> assertThat(repository.findAll().size()).isGreaterThanOrEqualTo(12));
    }

    @Test
    @SneakyThrows
    @DisplayName("Все записи за длительный период корректны и хронологичны")
    void shouldMaintainChronologicalOrderWithLargeData() {
        await().atMost(12, TimeUnit.SECONDS).untilAsserted(() -> {
            List<TimeEntry> all = repository.findAll();
            List<Instant> timestamps = all.stream()
                    .map(TimeEntry::getTimestamp)
                    .collect(Collectors.toList());

            List<Instant> sorted = timestamps.stream().sorted().toList();
            assertThat(timestamps).containsExactlyElementsOf(sorted);
        });
    }

    @Test
    @SneakyThrows
    @DisplayName("Проверка устойчивости при медленной БД")
    void shouldHandleSlowDatabaseWrites() {
        proxy.toxics()
                .latency("slow-db", ToxicDirection.DOWNSTREAM, 2000);

        TimeUnit.SECONDS.sleep(5);

        proxy.toxics().get("slow-db").remove();

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            List<TimeEntry> entries = repository.findAll();
            assertThat(entries).isNotEmpty();
        });
    }
}
