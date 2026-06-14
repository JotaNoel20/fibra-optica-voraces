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
        String url = "jdbc:postgresql://localhost:5432/fibra_optica_db";
        String user = "postgres"; // El usuario por defecto de Postgres suele ser este
        String password = "10132005";

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
