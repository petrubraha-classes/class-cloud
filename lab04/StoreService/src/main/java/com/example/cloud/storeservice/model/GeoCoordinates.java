package com.example.cloud.storeservice.model;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class GeoCoordinates {
    @NotNull
    @Min(-180)
    @Max(180)
    private Double longitude;

    @NotNull
    @Min(-90)
    @Max(90)
    private Double latitude;
}
