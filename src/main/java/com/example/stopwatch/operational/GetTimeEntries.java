package com.example.stopwatch.operational;

import com.example.stopwatch.infrastructure.repository.TimeEntryRepository;
import com.example.stopwatch.infrastructure.rest.dto.TimeEntryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetTimeEntries {

    private final TimeEntryRepository repository;

    public Page<TimeEntryDto> execute(Pageable pageable) {
        return repository.findAllByOrderByIdAsc(pageable)
                .map(entry -> new TimeEntryDto(entry.getId(), entry.getTimestamp()));
    }
}
