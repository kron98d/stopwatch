package com.example.stopwatch.infastructure.rest;

import com.example.stopwatch.AbstractIntegrationTest;
import com.example.stopwatch.domain.TimeEntry;
import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import com.example.stopwatch.infrastructure.rest.dto.TimeEntryDto;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.net.URI;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@TestPropertySource(properties = "spring.main.web-application-type=servlet")
public class StopwatchControllerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TimeEntryRepository repository;

    @Test
    @DisplayName("Контроллер: Возвращает страницу с временными записями")
    void shouldReturnPaginatedTimeEntries() {
        generateEntries(5);
        URI uri = URI.create("http://localhost:" + port + "/api/timestamp/all?page=0&size=3");

        ResponseEntity<PageResponse<TimeEntryDto>> response = restTemplate.exchange(
                uri,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getContent().size()).isEqualTo(3);
    }

    private void generateEntries(int count) {
        repository.deleteAll();
        List<TimeEntry> entries = IntStream.range(0, count)
                .mapToObj(i -> new TimeEntry(java.time.Instant.now().minusSeconds(i * 60L)))
                .toList();
        repository.saveAll(entries);
    }

    @JsonIgnoreProperties({"pageable", "sort", "last", "first", "numberOfElements", "empty"})
    public static class PageResponse<T> extends PageImpl<T> {
        public PageResponse() {
            super(List.of(), PageRequest.of(0, 1), 0);
        }
    }
}
