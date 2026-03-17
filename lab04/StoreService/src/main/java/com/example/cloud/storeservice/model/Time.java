package com.example.cloud.storeservice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class Time {
    @NotNull
    @Min(0)
    @Max(23)
    private Integer hour;

    @NotNull
    @Min(0)
    @Max(59)
    private Integer minute;
}
