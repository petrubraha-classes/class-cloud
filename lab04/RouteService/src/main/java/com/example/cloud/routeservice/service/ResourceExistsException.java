package com.example.cloud.routeservice.service;

public class ResourceExistsException extends Exception {
    public ResourceExistsException(String message) {
        super(message);
    }
}
