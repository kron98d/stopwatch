package com.example.stopwatch.infrastructure.rest.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@Schema(description = "DTO временной записи")
public record TimeEntryDto(
        @Schema(description = "ID записи", example = "1")
        long id,

        @Schema(description = "Момент времени записи", example = "2024-05-10T10:00:00Z")
        Instant timestamp
) {
}
