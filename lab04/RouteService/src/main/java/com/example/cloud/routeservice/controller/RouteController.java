package com.example.cloud.routeservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.cloud.routeservice.model.CreateRouteInput;
import com.example.cloud.routeservice.model.RouteSummary;
import com.example.cloud.routeservice.service.ResourceExistsException;
import com.example.cloud.routeservice.service.RouteService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/routes")
public class RouteController {

    private final RouteService routeService;

    public RouteController(RouteService routeService) {
        this.routeService = routeService;
    }

    @PostMapping
    public ResponseEntity<?> createRoute(
            @RequestHeader(value = "Authorization", required = false) String apiKey,
            @Valid @RequestBody CreateRouteInput input) {

        if (!routeService.isValidApiKey(apiKey)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Invalid or absent API key");
        }

        try {
            RouteSummary summary = routeService.createRoute(input);
            return ResponseEntity.ok(summary);
        } catch (ResourceExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Resource already exists");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal server error");
        }
    }
}
