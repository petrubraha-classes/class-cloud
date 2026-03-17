package com.example.cloud.routeservice.service;

import com.example.cloud.routeservice.model.CreateRouteInput;
import com.example.cloud.routeservice.model.RouteSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class RouteService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File dbFile = new File("config/db.json");
    private final File keysFile = new File("config/api-keys.json");

    private List<RouteSummary> database = new ArrayList<>();
    private List<String> apiKeys = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        if (dbFile.exists() && dbFile.length() > 0) {
            database = objectMapper.readValue(dbFile, new TypeReference<List<RouteSummary>>() {
            });
        }
        if (keysFile.exists() && keysFile.length() > 0) {
            apiKeys = objectMapper.readValue(keysFile, new TypeReference<List<String>>() {
            });
        }
    }

    public boolean isValidApiKey(String apiKey) {
        // usually it's "Bearer {key}", or directly the key
        // We will just check if the actual string contains any valid key,
        // or check for exact match.
        if (apiKey == null)
            return false;

        String cleanKey = apiKey.replace("Bearer ", "").trim();
        return apiKeys.contains(cleanKey) || apiKeys.contains(apiKey);
    }

    public synchronized RouteSummary createRoute(CreateRouteInput input) throws ResourceExistsException, IOException {
        for (RouteSummary existing : database) {
            if (existing.getStoreId().equals(input.getStoreId()) &&
                    existing.getStandIdList().equals(input.getStandIdList())) {
                throw new ResourceExistsException("Resource already exists.");
            }
        }

        RouteSummary summary = new RouteSummary();
        summary.setStoreId(input.getStoreId());
        summary.setRouteId(UUID.randomUUID());
        summary.setStandIdList(input.getStandIdList());
        summary.setSolutionList(new ArrayList<>());

        database.add(summary);

        objectMapper.writeValue(dbFile, database);

        return summary;
    }
}
