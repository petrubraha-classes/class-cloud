package com.example.cloud.storeservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cloud.storeservice.model.CreateStoreInput;
import com.example.cloud.storeservice.model.StoreSummary;
import com.example.cloud.storeservice.service.ResourceExistsException;
import com.example.cloud.storeservice.service.StoreService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping
    public ResponseEntity<?> createStore(
            @RequestHeader(value = "Authorization", required = false) String apiKey,
            @Valid @RequestBody CreateStoreInput input) {

        if (!storeService.isValidApiKey(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or absent API key");
        }

        try {
            StoreSummary summary = storeService.createStore(input);
            return ResponseEntity.ok(summary);
        } catch (ResourceExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Resource already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
