package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NodoRepository {

    private static final String COLUMNAS_NODO = """
            id, nombre, tipo, estado, capacidad_max, clientes_actuales,
            ST_Y(geom) AS latitud, ST_X(geom) AS longitud
            """;

    public NodoDTO guardar(NodoDTO nodo) {
        String sql = """
                INSERT INTO nodos
                    (nombre, tipo, estado, capacidad_max, clientes_actuales, geom)
                VALUES (?, ?, ?, ?, ?, ST_SetSRID(ST_MakePoint(?, ?), 4326))
                RETURNING id, nombre, tipo, estado, capacidad_max, clientes_actuales,
                          ST_Y(geom) AS latitud, ST_X(geom) AS longitud
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nodo.getNombre());
            statement.setString(2, nodo.getTipo());
            statement.setString(3, nodo.getEstado());
            statement.setObject(4, nodo.getCapacidadMax());
            statement.setObject(5, nodo.getClientesActuales());
            statement.setDouble(6, nodo.getLongitud());
            statement.setDouble(7, nodo.getLatitud());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearNodo(resultSet);
                }
                throw new SpatialException("No se pudo guardar el nodo.");
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al guardar el nodo.", e);
        }
    }

    public NodoDTO buscarPorId(int id) {
        String sql = "SELECT " + COLUMNAS_NODO + " FROM nodos WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapearNodo(resultSet) : null;
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al buscar el nodo con id " + id + ".", e);
        }
    }

    public List<NodoDTO> listarTodos() {
        String sql = "SELECT " + COLUMNAS_NODO + " FROM nodos ORDER BY id";
        List<NodoDTO> nodos = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                nodos.add(mapearNodo(resultSet));
            }
            return nodos;
        } catch (SQLException e) {
            throw new SpatialException("Error al listar los nodos.", e);
        }
    }

    public void eliminar(int id) {
        String sql = "DELETE FROM nodos WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SpatialException("Error al eliminar el nodo con id " + id + ".", e);
        }
    }

    private NodoDTO mapearNodo(ResultSet resultSet) throws SQLException {
        return new NodoDTO(
                resultSet.getObject("id", Integer.class),
                resultSet.getString("nombre"),
                resultSet.getString("tipo"),
                resultSet.getString("estado"),
                resultSet.getObject("capacidad_max", Integer.class),
                resultSet.getObject("clientes_actuales", Integer.class),
                resultSet.getDouble("latitud"),
                resultSet.getDouble("longitud")
        );
    }
}
