package com.example.cloud.storeservice.service;

public class ResourceExistsException extends Exception {
    public ResourceExistsException(String message) {
        super(message);
    }
}
