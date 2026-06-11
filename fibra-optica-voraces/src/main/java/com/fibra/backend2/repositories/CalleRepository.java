package com.fibra.backend2.repositories;

import com.fibra.backend2.config.DatabaseConnection;
import com.fibra.backend2.dto.CalleDTO;
import com.fibra.backend2.exceptions.SpatialException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CalleRepository {

    public List<CalleDTO> listarCalles() {
        String sql = "SELECT id, nombre, tipo_via, longitud FROM calles ORDER BY id";
        List<CalleDTO> calles = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                calles.add(mapearCalle(resultSet));
            }
            return calles;
        } catch (SQLException e) {
            throw new SpatialException("Error al listar las calles.", e);
        }
    }

    public boolean existeCalleCercana(double latitud, double longitud, double toleranciaMetros) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM calles
                    WHERE ST_DWithin(
                        geom::geography,
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                        ?
                    )
                )
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, longitud);
            statement.setDouble(2, latitud);
            statement.setDouble(3, toleranciaMetros);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() && resultSet.getBoolean(1);
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al validar si existe una calle cercana.", e);
        }
    }

    public double distanciaMinimaACalle(double latitud, double longitud) {
        String sql = """
                SELECT MIN(
                    ST_Distance(
                        geom::geography,
                        ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography
                    )
                ) AS distancia_minima
                FROM calles
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, longitud);
            statement.setDouble(2, latitud);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    double distancia = resultSet.getDouble("distancia_minima");
                    if (!resultSet.wasNull()) {
                        return distancia;
                    }
                }
                throw new SpatialException("No existen calles para calcular la distancia minima.");
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al calcular la distancia minima a una calle.", e);
        }
    }

    private CalleDTO mapearCalle(ResultSet resultSet) throws SQLException {
        return new CalleDTO(
                resultSet.getObject("id", Integer.class),
                resultSet.getString("nombre"),
                resultSet.getString("tipo_via"),
                resultSet.getObject("longitud", Double.class)
        );
    }
}
