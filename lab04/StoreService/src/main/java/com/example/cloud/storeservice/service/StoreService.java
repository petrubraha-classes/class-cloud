package com.example.cloud.storeservice.service;

import com.example.cloud.storeservice.model.CreateStoreInput;
import com.example.cloud.storeservice.model.StoreSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.time.Instant;

@Service
public class StoreService {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    private final File dbFile = new File("config/db.json");
    private final File keysFile = new File("config/api-keys.json");

    private List<StoreSummary> database = new ArrayList<>();
    private List<String> apiKeys = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        if (dbFile.exists() && dbFile.length() > 0) {
            database = objectMapper.readValue(dbFile, new TypeReference<List<StoreSummary>>() {
            });
        }
        if (keysFile.exists() && keysFile.length() > 0) {
            apiKeys = objectMapper.readValue(keysFile, new TypeReference<List<String>>() {
            });
        }
    }

    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null)
            return false;

        String cleanKey = apiKey.replace("Bearer ", "").trim();
        return apiKeys.contains(cleanKey) || apiKeys.contains(apiKey);
    }

    public synchronized StoreSummary createStore(CreateStoreInput input) throws ResourceExistsException, IOException {
        for (StoreSummary existing : database) {
            if (existing.getName() != null && existing.getName().equals(input.getName()) &&
                    existing.getBrandId() != null && existing.getBrandId().equals(input.getBrandId())) {
                throw new ResourceExistsException("Resource already exists.");
            }
        }

        StoreSummary summary = new StoreSummary();
        summary.setStoreId(UUID.randomUUID());
        summary.setName(input.getName());
        summary.setBrandId(input.getBrandId());
        summary.setDescription(input.getDescription());
        summary.setImageUrl(input.getImageUrl());
        summary.setGeoCoordinates(input.getGeoCoordinates());
        summary.setOperatingHoursMap(input.getOperatingHoursMap());
        summary.setTimezone(input.getTimezone());
        summary.setCreatedAt(Instant.now());
        summary.setUpdatedAt(Instant.now());

        database.add(summary);
        objectMapper.writeValue(dbFile, database);

        return summary;
    }
}
