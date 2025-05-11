package com.example.stopwatch.operational;

import com.example.stopwatch.AbstractIntegrationTest;
import com.example.stopwatch.domain.TimeEntry;
import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import com.example.stopwatch.infrastructure.rest.dto.TimeEntryDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class GetTimeEntriesTest extends AbstractIntegrationTest {

    @Autowired
    private GetTimeEntries getTimeEntries;

    @Autowired
    private TimeEntryRepository repository;

    @Test
    @DisplayName("Возвращает страницу с DTO временных записей")
    void shouldReturnMappedDtoPage() {
        generateEntries(3);

        Page<TimeEntryDto> result = getTimeEntries.execute(PageRequest.of(0, 2));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0)).isInstanceOf(TimeEntryDto.class);
    }

    @Test
    @DisplayName("Возвращает пустую страницу при отсутствии записей")
    void shouldReturnEmptyPageWhenNoEntries() {
        generateEntries(0);
        Page<TimeEntryDto> result = getTimeEntries.execute(PageRequest.of(0, 10));

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Корректно разбивает записи по страницам")
    void shouldPaginateCorrectly() {
        generateEntries(25);
        Page<TimeEntryDto> page1 = getTimeEntries.execute(PageRequest.of(0, 10));
        Page<TimeEntryDto> page2 = getTimeEntries.execute(PageRequest.of(1, 10));
        Page<TimeEntryDto> page3 = getTimeEntries.execute(PageRequest.of(2, 10));

        assertThat(page1.getContent()).hasSize(10);
        assertThat(page2.getContent()).hasSize(10);
        assertThat(page3.getContent()).hasSize(5);
        assertThat(page1.getTotalElements()).isEqualTo(25);
    }

    private void generateEntries(int count) {
        repository.deleteAll();
        List<TimeEntry> entries = IntStream.range(0, count)
                .mapToObj(i -> new TimeEntry(Instant.now().minusSeconds(i * 60L)))
                .toList();
        repository.saveAll(entries);
    }
}
