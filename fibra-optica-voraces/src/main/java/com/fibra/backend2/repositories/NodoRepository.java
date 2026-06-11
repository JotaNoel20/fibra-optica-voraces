package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.NodoDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class NodoRepository {

    private final DatabaseConnection databaseConnection;

    public NodoRepository(DatabaseConnection databaseConnection) {
        this.databaseConnection = databaseConnection;
    }

    public NodoDTO guardar(NodoDTO nodo) {
        return nodo;
    }

    public Optional<NodoDTO> buscarPorId(int id) {
        return Optional.empty();
    }

    public List<NodoDTO> listarTodos() {
        return Collections.emptyList();
    }

    public boolean eliminar(int id) {
        return false;
    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }
}
