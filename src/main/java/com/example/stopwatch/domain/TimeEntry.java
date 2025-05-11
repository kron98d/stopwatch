package com.example.stopwatch.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.Instant;

@Entity
@NoArgsConstructor
@Getter
@ToString
@EqualsAndHashCode(of = "id")
public class TimeEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id = 0;
    @Column(nullable = false)
    private Instant timestamp;

    public TimeEntry(Instant timestamp) {
        this.timestamp = timestamp;
    }
}