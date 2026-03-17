package com.example.cloud.routeservice.model;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRouteInput {
    @NotNull
    private UUID storeId;

    @NotNull
    @NotEmpty
    private List<UUID> standIdList;
}
