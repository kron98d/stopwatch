package com.example.stopwatch.infrastructure.rest;

import com.example.stopwatch.infrastructure.rest.dto.TimeEntryDto;
import com.example.stopwatch.operational.GetTimeEntries;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Записи времени", description = "Операции получения временных записей из базы данных")
@RestController
@RequestMapping("/api/timestamp")
@RequiredArgsConstructor
public class StopwatchController {

    private final GetTimeEntries getTimeEntries;

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешный ответ"),
            @ApiResponse(responseCode = "400", description = "Некорректные параметры запроса"),
            @ApiResponse(responseCode = "500", description = "Ошибка сервера")
    })
    @Operation(summary = "Получить список временных записей с пагинацией")
    @GetMapping("/all")
    public Page<TimeEntryDto> getEntries(@PageableDefault(size = 100, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return getTimeEntries.execute(pageable);
    }
}
