package com.dining.fluxo.db;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {
    private static DatabaseConnection instance;
    private Connection connection;

    private DatabaseConnection() {
        String dbUrl = loadDatabaseUrl();
        try {
            Class.forName("org.postgresql.Driver");
            this.connection = DriverManager.getConnection(dbUrl);
            initializeDatabase();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Failed to connect to the database.");
            e.printStackTrace();
        }
    }

    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private String loadDatabaseUrl() {
        try (BufferedReader reader = new BufferedReader(new FileReader(".env"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("DATABASE_URL=")) {
                    String url = line.substring("DATABASE_URL=".length()).trim();
                    if (url.startsWith("\"") && url.endsWith("\"")) {
                        url = url.substring(1, url.length() - 1);
                    }
                    return url;
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to read .env file. Ensure it exists in the root directory.");
        }
        return "";
    }

    private void initializeDatabase() {
        String createTableSql = "CREATE TABLE IF NOT EXISTS tables (" +
                "id SERIAL PRIMARY KEY, " +
                "number INT UNIQUE NOT NULL, " +
                "capacity INT NOT NULL" +
                ")";

        String createWaiterSql = "CREATE TABLE IF NOT EXISTS waiters (" +
                "id SERIAL PRIMARY KEY, " +
                "name VARCHAR(255) NOT NULL, " +
                "email VARCHAR(255) UNIQUE NOT NULL" +
                ")";

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createTableSql);
            stmt.execute(createWaiterSql);
            System.out.println("Database initialized successfully.");
        } catch (SQLException e) {
            System.err.println("Failed to initialize database tables.");
            e.printStackTrace();
        }
    }
}
