package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.CalleDTO;

import java.util.Collections;
import java.util.List;

public class CalleRepository {

    private final DatabaseConnection databaseConnection;

    public CalleRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public List<CalleDTO> listarCalles() {
        return Collections.emptyList();
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
