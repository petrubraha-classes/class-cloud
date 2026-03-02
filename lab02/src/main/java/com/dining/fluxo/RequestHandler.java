package com.dining.fluxo;

import com.dining.fluxo.resources.RestResource;
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

public class RequestHandler<R extends RestResource> implements HttpHandler {
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
            String uri = exchange.getRequestURI().getPath(); // Used getPath() to omit query params if any

            System.out.println("--- New Request ---");
            System.out.println(method + " " + uri);

            System.out.println("Headers:");
            for (Map.Entry<String, List<String>> header : exchange.getRequestHeaders().entrySet()) {
                System.out.println("  " + header.getKey() + ": " + String.join(", ", header.getValue()));
            }

            InputStream is = exchange.getRequestBody();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            String body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);

            System.out.println("Payload: ");
            System.out.println(body.isEmpty() ? "<empty>" : body);
            System.out.println("-------------------");

            RestResource resourceInstance = resourceClass.getDeclaredConstructor().newInstance();

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
                    responseBytes = resourceInstance.resolveGet(id);
                    break;
                case "POST":
                    if (id != null) {
                        throw new ResourceNotFoundException("POST should be at root resource context, not on an ID");
                    }
                    responseBytes = resourceInstance.resolvePost(body);
                    break;
                case "PUT":
                    responseBytes = resourceInstance.resolvePut(id, body);
                    break;
                case "DELETE":
                    responseBytes = resourceInstance.resolveDelete(id);
                    break;
                default:
                    throw new InvalidInputException("Unsupported method");
            }

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

    private byte[] createErrorResponse(String error, String message) {
        try {
            return MAPPER.writeValueAsBytes(Map.of("error", error, "message", message));
        } catch (Exception e) {
            return "{\"error\":\"InternalServerError\"}".getBytes(StandardCharsets.UTF_8);
        }
    }
}
