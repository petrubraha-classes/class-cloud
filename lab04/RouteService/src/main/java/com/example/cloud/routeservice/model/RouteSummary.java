package com.example.cloud.routeservice.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteSummary {
    @NotNull
    private UUID storeId;

    @NotNull
    private UUID routeId;

    @NotNull
    private List<UUID> standIdList;

    @NotNull
    private List<Solution> solutionList;
}
