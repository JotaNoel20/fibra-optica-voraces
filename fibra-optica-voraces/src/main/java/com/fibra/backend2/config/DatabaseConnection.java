package com.fibra.backend2.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        String url = System.getenv("DB_URL");
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");

        List<String> missingVariables = new ArrayList<>();
        if (isBlank(url)) {
            missingVariables.add("DB_URL");
        }
        if (isBlank(user)) {
            missingVariables.add("DB_USER");
        }
        if (isBlank(password)) {
            missingVariables.add("DB_PASSWORD");
        }

        if (!missingVariables.isEmpty()) {
            throw new IllegalStateException(
                    "Faltan variables de entorno requeridas para la conexion a PostgreSQL: "
                            + String.join(", ", missingVariables)
            );
        }

        return DriverManager.getConnection(url, user, password);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
