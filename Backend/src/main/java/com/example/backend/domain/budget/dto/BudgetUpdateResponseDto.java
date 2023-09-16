package com.example.backend.domain.budget.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder(toBuilder = true)
public class BudgetUpdateResponseDto {
    private Long planId;
    private String category;
    private Long amount;
    @JsonFormat(pattern = "yyyyMMdd")
    private LocalDate travelDate;
}
