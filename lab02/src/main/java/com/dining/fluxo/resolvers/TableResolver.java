package com.dining.fluxo.resolvers;

import com.dining.fluxo.db.DatabaseConnection;
import com.dining.fluxo.exceptions.InvalidInputException;
import com.dining.fluxo.exceptions.ResourceAlreadyExistsException;
import com.dining.fluxo.exceptions.ResourceNotFoundException;
import com.dining.fluxo.models.Table;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TableResolver implements RestResolver {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private Connection connection;

    public TableResolver() {
        this.connection = DatabaseConnection.getInstance().getConnection();
    }

    @Override
    public byte[] resolvePost(String payload) throws Exception {
        if (payload == null || payload.trim().isEmpty()) {
            throw new InvalidInputException("Payload cannot be empty");
        }

        Table table;
        try {
            table = MAPPER.readValue(payload, Table.class);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid JSON payload");
        }

        if (table.getNumber() == null || table.getCapacity() == null) {
            throw new InvalidInputException("Table number and capacity are required");
        }

        String checkSql = "SELECT id FROM tables WHERE number = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, table.getNumber());
            var rs = checkStmt.executeQuery();
            if (rs.next()) {
                throw new ResourceAlreadyExistsException("Table with number " + table.getNumber() + " already exists");
            }
        }

        String insertSql = "INSERT INTO tables (number, capacity) VALUES (?, ?) RETURNING id, number, capacity";
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
            insertStmt.setInt(1, table.getNumber());
            insertStmt.setInt(2, table.getCapacity());
            ResultSet rs = insertStmt.executeQuery();
            if (rs.next()) {
                Table created = new Table(rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity"));
                return MAPPER.writeValueAsBytes(created);
            }
        }

        throw new RuntimeException("Failed to create table");
    }

    @Override
    public byte[] resolveGet(Integer id) throws Exception {
        if (id == null) {
            // Get all
            String sql = "SELECT id, number, capacity FROM tables";
            List<Table> tables = new ArrayList<>();
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    tables.add(new Table(rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity")));
                }
            }
            return MAPPER.writeValueAsBytes(tables);
        } else {
            // Get one
            String sql = "SELECT id, number, capacity FROM tables WHERE id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                stmt.setInt(1, id);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    Table table = new Table(rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity"));
                    return MAPPER.writeValueAsBytes(table);
                } else {
                    throw new ResourceNotFoundException("Table with ID " + id + " not found");
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

        Table table;
        try {
            table = MAPPER.readValue(payload, Table.class);
        } catch (Exception e) {
            throw new InvalidInputException("Invalid JSON payload");
        }

        if (table.getNumber() == null || table.getCapacity() == null) {
            throw new InvalidInputException("Table number and capacity are required");
        }

        // Check if ID exists, if not, create it
        boolean exists = false;
        String checkSql = "SELECT id FROM tables WHERE id = ?";
        try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
            checkStmt.setInt(1, id);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                exists = true;
            }
        }

        if (exists) {
            String updateSql = "UPDATE tables SET number = ?, capacity = ? WHERE id = ? RETURNING id, number, capacity";
            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                updateStmt.setInt(1, table.getNumber());
                updateStmt.setInt(2, table.getCapacity());
                updateStmt.setInt(3, id);
                ResultSet rs = updateStmt.executeQuery();
                if (rs.next()) {
                    Table updated = new Table(rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity"));
                    return MAPPER.writeValueAsBytes(updated);
                }
            }
        } else {
            String insertSql = "INSERT INTO tables (id, number, capacity) VALUES (?, ?, ?) RETURNING id, number, capacity";
            try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                insertStmt.setInt(1, id);
                insertStmt.setInt(2, table.getNumber());
                insertStmt.setInt(3, table.getCapacity());
                ResultSet rs = insertStmt.executeQuery();
                if (rs.next()) {
                    Table created = new Table(rs.getInt("id"), rs.getInt("number"), rs.getInt("capacity"));
                    return MAPPER.writeValueAsBytes(created);
                }
            }
        }
        throw new RuntimeException("Failed to PUT table");
    }

    @Override
    public byte[] resolveDelete(Integer id) throws Exception {
        if (id == null) {
            throw new InvalidInputException("ID is required for DELETE");
        }

        String deleteSql = "DELETE FROM tables WHERE id = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSql)) {
            deleteStmt.setInt(1, id);
            int rowsAffected = deleteStmt.executeUpdate();
            if (rowsAffected == 0) {
                // To be idempotent, deleting a non-existent resource usually returns 200 or
                // 204.
                // We'll return empty bytes for success.
            }
        }
        return new byte[0];
    }
}
