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
public class TspEdge {
    @NotNull
    private UUID edgeId;

    @NotNull
    private UUID startingNodeId;

    private List<StandNode> standNodeList;
}
