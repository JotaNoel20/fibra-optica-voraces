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

    private static final boolean DEBUG = false;

    public ConexionDTO guardar(ConexionDTO conexion) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.guardar ===");
            System.out.println("  Origen: " + conexion.getOrigenId() + " | Destino: " + conexion.getDestinoId() + " | Distancia: " + conexion.getDistancia());
        }
        
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

    public int guardarBatch(List<ConexionDTO> conexiones) {
        if (conexiones == null || conexiones.isEmpty()) {
            return 0;
        }
        
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.guardarBatch ===");
            System.out.println("  Conexiones a guardar: " + conexiones.size());
        }
        
        String sql = "INSERT INTO conexiones (origen_id, destino_id, distancia) VALUES (?, ?, ?)";
        int guardadas = 0;
        
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            
            connection.setAutoCommit(false);
            
            for (ConexionDTO conexion : conexiones) {
                statement.setObject(1, conexion.getOrigenId());
                statement.setObject(2, conexion.getDestinoId());
                statement.setObject(3, conexion.getDistancia());
                statement.addBatch();
                guardadas++;
            }
            
            statement.executeBatch();
            connection.commit();
            
            if (DEBUG) {
                System.out.println("  Conexiones guardadas: " + guardadas);
            }
            
        } catch (SQLException e) {
            throw new SpatialException("Error al guardar batch de conexiones.", e);
        }
        
        return guardadas;
    }

    public List<ConexionDTO> listarTodas() {
        if (DEBUG) System.out.println("\n=== ConexionRepository.listarTodas ===");
        
        String sql = "SELECT id, origen_id, destino_id, distancia FROM conexiones ORDER BY id";
        List<ConexionDTO> conexiones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                conexiones.add(mapearConexion(resultSet));
            }
            if (DEBUG) System.out.println("  Conexiones encontradas: " + conexiones.size());
            return conexiones;
        } catch (SQLException e) {
            throw new SpatialException("Error al listar las conexiones.", e);
        }
    }

    public ConexionDTO buscarPorId(int id) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.buscarPorId ===");
            System.out.println("  Buscando conexión con ID: " + id);
        }
        
        String sql = "SELECT id, origen_id, destino_id, distancia FROM conexiones WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearConexion(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al buscar la conexion con id " + id, e);
        }
    }

    public List<ConexionDTO> buscarPorNodo(int nodoId) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.buscarPorNodo ===");
            System.out.println("  Buscando conexiones del nodo: " + nodoId);
        }
        
        String sql = "SELECT id, origen_id, destino_id, distancia FROM conexiones WHERE origen_id = ? OR destino_id = ? ORDER BY id";
        List<ConexionDTO> conexiones = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nodoId);
            statement.setInt(2, nodoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    conexiones.add(mapearConexion(resultSet));
                }
            }
            if (DEBUG) System.out.println("  Conexiones encontradas: " + conexiones.size());
            return conexiones;
        } catch (SQLException e) {
            throw new SpatialException("Error al buscar conexiones del nodo " + nodoId, e);
        }
    }

    public ConexionDTO buscarPorNodos(int nodoA, int nodoB) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.buscarPorNodos ===");
            System.out.println("  Buscando conexión entre " + nodoA + " y " + nodoB);
        }
        
        String sql = "SELECT id, origen_id, destino_id, distancia FROM conexiones WHERE (origen_id = ? AND destino_id = ?) OR (origen_id = ? AND destino_id = ?)";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nodoA);
            statement.setInt(2, nodoB);
            statement.setInt(3, nodoB);
            statement.setInt(4, nodoA);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapearConexion(resultSet);
                }
                return null;
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al buscar conexion entre " + nodoA + " y " + nodoB, e);
        }
    }

    public void eliminar(int id) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.eliminar ===");
            System.out.println("  Eliminando conexión con ID: " + id);
        }
        
        String sql = "DELETE FROM conexiones WHERE id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SpatialException("Error al eliminar la conexion con id " + id, e);
        }
    }

    public void eliminarPorNodo(int nodoId) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.eliminarPorNodo ===");
            System.out.println("  Eliminando conexiones del nodo: " + nodoId);
        }
        
        String sql = "DELETE FROM conexiones WHERE origen_id = ? OR destino_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nodoId);
            statement.setInt(2, nodoId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SpatialException("Error al eliminar conexiones del nodo " + nodoId, e);
        }
    }

    public int contarConexionesDeNodo(int nodoId) {
        if (DEBUG) {
            System.out.println("\n=== ConexionRepository.contarConexionesDeNodo ===");
            System.out.println("  Contando conexiones del nodo: " + nodoId);
        }
        
        String sql = "SELECT COUNT(*) AS total FROM conexiones WHERE origen_id = ? OR destino_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, nodoId);
            statement.setInt(2, nodoId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("total");
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new SpatialException("Error al contar conexiones del nodo " + nodoId, e);
        }
    }

    public void eliminarTodas() {
        if (DEBUG) System.out.println("\n=== ConexionRepository.eliminarTodas ===");
        
        String sql = "DELETE FROM conexiones";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new SpatialException("Error al eliminar todas las conexiones", e);
        }
    }

    private ConexionDTO mapearConexion(ResultSet resultSet) throws SQLException {
        return new ConexionDTO(
                resultSet.getObject("id", Integer.class),
                resultSet.getObject("origen_id", Integer.class),
                resultSet.getObject("destino_id", Integer.class),
                resultSet.getDouble("distancia")
        );
    }
}