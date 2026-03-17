package com.example.cloud.storeservice.model;

import lombok.Data;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
public class StoreSummary {
    private UUID storeId;
    private String name;
    private UUID brandId;
    private String description;
    private String imageUrl;
    private GeoCoordinates geoCoordinates;
    private Map<DayType, TimeRange> operatingHoursMap;
    private Double timezone;
    private Instant createdAt;
    private Instant updatedAt;
}
