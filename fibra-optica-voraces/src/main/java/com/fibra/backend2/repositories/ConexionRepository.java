package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.exceptions.SpatialException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConexionRepository {

    public ConexionDTO guardar(ConexionDTO conexion) {
        String sql = """
                INSERT INTO conexiones (origen_id, destino_id, distancia)
                VALUES (?, ?, ?)
                RETURNING id, origen_id, destino_id, distancia
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, conexion.getOrigenId());
            statement.setObject(2, conexion.getDestinoId());
            statement.setObject(3, conexion.getDistancia());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearConexion(resultSet);
                }
                throw new SpatialException("No se pudo guardar la conexion.");
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al guardar la conexion.", e);
        }
    }

    public List<ConexionDTO> listarTodas() {
        String sql = """
                SELECT id, origen_id, destino_id, distancia
                FROM conexiones
                ORDER BY id
                """;
        List<ConexionDTO> conexiones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                conexiones.add(mapearConexion(resultSet));
            }
            return conexiones;
        } catch (SQLException e) {
            throw new SpatialException("Error al listar las conexiones.", e);
        }
    }

    private ConexionDTO mapearConexion(ResultSet resultSet) throws SQLException {
        return new ConexionDTO(
                resultSet.getObject("id", Integer.class),
                resultSet.getObject("origen_id", Integer.class),
                resultSet.getObject("destino_id", Integer.class),
                resultSet.getObject("distancia", Double.class)
        );
    }
}
