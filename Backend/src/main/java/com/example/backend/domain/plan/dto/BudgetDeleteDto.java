package com.example.backend.domain.plan.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BudgetDeleteDto {
    private String category;
    private LocalDateTime travel_date;
}
