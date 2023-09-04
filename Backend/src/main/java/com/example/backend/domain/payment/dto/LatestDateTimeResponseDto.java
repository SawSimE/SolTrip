package com.example.backend.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder(toBuilder = true)
public class LatestDateTimeResponseDto {
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate transactionDate;
    @DateTimeFormat(pattern = "HH:mm")
    private LocalTime transactionTime;
}