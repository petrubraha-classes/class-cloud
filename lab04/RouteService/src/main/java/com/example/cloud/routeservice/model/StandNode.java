package com.example.cloud.routeservice.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StandNode {
    @NotNull
    private UUID standId;

    private Double weight;
}
