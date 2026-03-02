package com.dining.fluxo.resolvers;

import com.dining.fluxo.db.DatabaseConnection;
import com.dining.fluxo.exceptions.InvalidInputException;
import com.dining.fluxo.exceptions.ResourceAlreadyExistsException;
import com.dining.fluxo.exceptions.ResourceNotFoundException;
import com.dining.fluxo.models.Waiter;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WaiterResolver implements RestResolver {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Connection connection;

    public WaiterResolver() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public byte[] resolvePost(String payload) throws Exception {
        if (payload == null || payload.trim().isEmpty()) {
            throw new InvalidInputException("Payload cannot be empty");
        }

        Waiter waiter;
        try {
            waiter = MAPPER.readValue(payload, Waiter.class);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid JSON payload");
        }

        if (waiter.getName() == null || waiter.getName().trim().isEmpty() || waiter.getEmail() == null
                || waiter.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Waiter name and email are required");
        }

        String checkSql = "SELECT id FROM waiters WHERE email = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setString(1, waiter.getEmail());
            var rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new ResourceAlreadyExistsException("Waiter with email " + waiter.getEmail() + " already exists");
            }
        }

        String insertSql = "INSERT INTO waiters (name, email) VALUES (?, ?) RETURNING id, name, email";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setString(1, waiter.getName());
            insertStmt.setString(2, waiter.getEmail());
            ResultSet rs = insertStmt.executeQuery();
            if (rs.next()) {
                Waiter created = new Waiter(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                return MAPPER.writeValueAsBytes(created);
            }
        }

        throw new RuntimeException("Failed to create waiter");
    }

    @Override
    public byte[] resolveGet(Integer id) throws Exception {
        if (id == null) {
            // Get all
            String sql = "SELECT id, name, email FROM waiters";
            List<Waiter> waiters = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    waiters.add(new Waiter(rs.getInt("id"), rs.getString("name"), rs.getString("email")));
                }
            }
            return MAPPER.writeValueAsBytes(waiters);
        } else {
            // Get one
            String sql = "SELECT id, name, email FROM waiters WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Waiter waiter = new Waiter(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                    return MAPPER.writeValueAsBytes(waiter);
                } else {
                    throw new ResourceNotFoundException("Waiter with ID " + id + " not found");
                }
            }
        }
    }

    @Override
    public byte[] resolvePut(Integer id, String payload) throws Exception {
        if (id == null) {
            throw new InvalidInputException("ID is required for PUT");
        }
        if (payload == null || payload.trim().isEmpty()) {
            throw new InvalidInputException("Payload cannot be empty");
        }

        Waiter waiter;
        try {
            waiter = MAPPER.readValue(payload, Waiter.class);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid JSON payload");
        }

        if (waiter.getName() == null || waiter.getName().trim().isEmpty() || waiter.getEmail() == null
                || waiter.getEmail().trim().isEmpty()) {
            throw new InvalidInputException("Waiter name and email are required");
        }

        // Check if ID exists, if not, create it
        boolean exists = false;
        String checkSql = "SELECT id FROM waiters WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        }

        if (exists) {
            String updateSql = "UPDATE waiters SET name = ?, email = ? WHERE id = ? RETURNING id, name, email";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setString(1, waiter.getName());
                updateStmt.setString(2, waiter.getEmail());
                updateStmt.setInt(3, id);
                ResultSet rs = updateStmt.executeQuery();
                if (rs.next()) {
                    Waiter updated = new Waiter(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                    return MAPPER.writeValueAsBytes(updated);
                }
            }
        } else {
            String insertSql = "INSERT INTO waiters (id, name, email) VALUES (?, ?, ?) RETURNING id, name, email";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, id);
                insertStmt.setString(2, waiter.getName());
                insertStmt.setString(3, waiter.getEmail());
                ResultSet rs = insertStmt.executeQuery();
                if (rs.next()) {
                    Waiter created = new Waiter(rs.getInt("id"), rs.getString("name"), rs.getString("email"));
                    return MAPPER.writeValueAsBytes(created);
                }
            }
        }
        throw new RuntimeException("Failed to PUT waiter");
    }

    @Override
    public byte[] resolveDelete(Integer id) throws Exception {
        if (id == null) {
            throw new InvalidInputException("ID is required for DELETE");
        }

        String deleteSql = "DELETE FROM waiters WHERE id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, id);
            int rowsAffected = deleteStmt.executeUpdate();
            // Idempotent DELETE does not fail if item missing.
        }
        return new byte[0];
    }
}
