package com.example.cloud.storeservice.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class CreateStoreInput {
    @Pattern(regexp = "^[a-zA-Z0-9\\- ]+$")
    @Size(min = 3, max = 63)
    private String name;

    @NotNull
    private UUID brandId;

    @Pattern(regexp = "^[a-zA-Z0-9\\-, ]+$")
    @Size(min = 8, max = 255)
    private String description;

    @Pattern(regexp = "^https?://[a-zA-Z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]+\\.(jpg|jpeg|png|gif)$")
    @Size(min = 8, max = 255)
    private String imageUrl;

    @Min(-12)
    @Max(14)
    private Double timezone;

    private Map<DayType, @Valid TimeRange> operatingHoursMap;

    @Valid
    private GeoCoordinates geoCoordinates;
}
