package com.example.stopwatch.infastructure.repository;

import com.example.stopwatch.AbstractIntegrationTest;
import com.example.stopwatch.domain.TimeEntry;
import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import lombok.SneakyThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TimeEntryRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private TimeEntryRepository repository;

    @Test
    @DisplayName("Сохраняет и извлекает запись по ID")
    void shouldSaveAndFindById() {
        TimeEntry entry = new TimeEntry(Instant.now());
        TimeEntry saved = repository.save(entry);

        Optional<TimeEntry> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTimestamp()).isEqualTo(entry.getTimestamp());
    }

    @Test
    @DisplayName("Удаляет все записи и возвращает пустую коллекцию")
    void shouldDeleteAllAndBeEmpty() {
        repository.save(new TimeEntry(Instant.now()));
        repository.deleteAll();

        List<TimeEntry> all = repository.findAll();

        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("Возвращает все записи по возрастанию ID (findAllByOrderByIdAsc)")
    void shouldReturnAllOrderedByIdAsc() {
        repository.deleteAll();

        TimeEntry e1 = new TimeEntry(Instant.now().minusSeconds(120));
        TimeEntry e2 = new TimeEntry(Instant.now().minusSeconds(60));
        TimeEntry e3 = new TimeEntry(Instant.now());

        repository.saveAll(List.of(e1, e2, e3));

        List<TimeEntry> result = repository.findAllByOrderByIdAsc(PageRequest.of(0, 10)).getContent();

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getTimestamp()).isEqualTo(e1.getTimestamp());
        assertThat(result.get(1).getTimestamp()).isEqualTo(e2.getTimestamp());
        assertThat(result.get(2).getTimestamp()).isEqualTo(e3.getTimestamp());
    }

    @Test
    @SneakyThrows
    @DisplayName("Пагинирует записи корректно")
    void shouldPaginateCorrectly() {
        repository.deleteAll();

        for (int i = 0; i < 20; i++) {
            repository.save(new TimeEntry(Instant.now().minusSeconds(i * 60L)));
        }

        Page<TimeEntry> page1 = repository.findAllByOrderByIdAsc(PageRequest.of(0, 5));
        Page<TimeEntry> page2 = repository.findAllByOrderByIdAsc(PageRequest.of(1, 5));

        assertThat(page1.getContent()).hasSize(5);
        assertThat(page2.getContent()).hasSize(5);
    }
}