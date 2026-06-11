package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.ConexionDTO;

import java.util.Collections;
import java.util.List;

public class ConexionRepository {

    private final DatabaseConnection databaseConnection;

    public ConexionRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public ConexionDTO guardar(ConexionDTO conexion) {
        return conexion;
    }

    public List<ConexionDTO> listarTodas() {
        return Collections.emptyList();
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
