package com.example.cloud.storeservice.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TimeRange {
    @NotNull
    private Time begin;

    @NotNull
    private Time end;
}
