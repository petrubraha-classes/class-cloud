package com.dining.fluxo;

import com.dining.fluxo.resolvers.RestResolver;
import com.dining.fluxo.exceptions.InvalidInputException;
import com.dining.fluxo.exceptions.ResourceAlreadyExistsException;
import com.dining.fluxo.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class RequestHandler<R extends RestResolver> implements HttpHandler {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final Class<R> resourceClass;

    public RequestHandler(Class<R> resourceClass) {
        this.resourceClass = resourceClass;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int statusCode = 200;
        byte[] responseBytes = new byte[0];

        try {
            String method = exchange.getRequestMethod();
            String uri = exchange.getRequestURI().getPath();
            String body = extractExangeBody(exchange.getRequestBody());

            printRequestHead(method, uri, exchange.getRequestHeaders());
            printRequestBody(body);

            resolveRequest(method, uri, body);

        } catch (InvalidInputException e) {
            statusCode = 400;
            responseBytes = createErrorResponse("InvalidInputError", e.getMessage());
        } catch (ResourceNotFoundException e) {
            statusCode = 404;
            responseBytes = createErrorResponse("ResourceNotFoundError", e.getMessage());
        } catch (ResourceAlreadyExistsException e) {
            statusCode = 409;
            responseBytes = createErrorResponse("ResourceAlreadyExistsError", e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = 500;
            responseBytes = createErrorResponse("InternalServerError", "An internal error occurred");
        }

        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, responseBytes.length == 0 ? -1 : responseBytes.length);

        if (responseBytes.length > 0) {
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(responseBytes);
            }
        } else {
            exchange.getResponseBody().close();
        }
    }

    private String extractExangeBody(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return new String(buffer.toByteArray(), StandardCharsets.UTF_8);
    }

    private byte[] resolveRequest(String method, String uri, String body) throws IOException, Exception {
        RestResolver resourceInstance = resourceClass.getDeclaredConstructor().newInstance();

        Integer id = null;
        String[] parts = uri.split("/");
        if (parts.length > 2) {
            try {
                id = Integer.parseInt(parts[2]);
            } catch (NumberFormatException e) {
                throw new InvalidInputException("Invalid ID format in URI");
            }
        }

        switch (method.toUpperCase()) {
            case "GET":
                return resourceInstance.resolveGet(id);
            case "POST":
                if (id != null) {
                    throw new ResourceNotFoundException("POST should be at root resource context, not on an ID");
                }
                return resourceInstance.resolvePost(body);
            case "PUT":
                return resourceInstance.resolvePut(id, body);
            case "DELETE":
                return resourceInstance.resolveDelete(id);
            default:
                throw new InvalidInputException("Unsupported method");
        }
    }

    private byte[] createErrorResponse(String error, String message) {
        try {
            return MAPPER.writeValueAsBytes(Map.of("error", error, "message", message));
        } catch (Exception e) {
            return "{\"error\":\"InternalServerError\"}".getBytes(StandardCharsets.UTF_8);
        }
    }

    private void printRequestHead(String method, String uri, Map<String, List<String>> headers) {
        System.out.println("--- New Request ---");
        System.out.printf("%s %s\n", method, uri);
        System.out.println("Headers:");
        for (Map.Entry<String, List<String>> header : headers.entrySet()) {
            System.out.printf("  %s: %s\n", header.getKey(), String.join(", ", header.getValue()));
        }
    }

    private void printRequestBody(String body) {
        System.out.println("Payload: ");
        System.out.println(body.isEmpty() ? "<empty>" : body);
        System.out.println("-------------------");
    }
}
