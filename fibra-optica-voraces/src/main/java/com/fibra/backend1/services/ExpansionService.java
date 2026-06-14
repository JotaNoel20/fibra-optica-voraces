package com.fibra.backend1.services;

import com.fibra.backend1.graph.Nodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend2.repositories.CalleRepository;

import java.util.ArrayList;
import java.util.List;

public class ExpansionService {

    // Constantes de distancias máximas (en metros)
    private static final double DISTANCIA_MAX_POSTE_CLIENTE = 40.0;
    private static final double DISTANCIA_MAX_POSTE_POSTE = 80.0;
    private static final double DISTANCIA_MAX_CENTRAL_POSTE = 150.0;
    
    // Capacidad estándar para postes sugeridos
    private static final int CAPACIDAD_SUGERIDO = 16;
    
    private CalleRepository calleRepository;
    
    // Constructor original (sin repositorio) - para compatibilidad
    public ExpansionService() {
        this.calleRepository = null;
    }
    
    // Nuevo constructor con repositorio para proyección sobre calles
    public ExpansionService(CalleRepository calleRepository) {
        this.calleRepository = calleRepository;
    }

    /**
     * Genera postes sugeridos entre un origen y un destino cuando la distancia
     * excede el límite permitido.
     * Si hay CalleRepository disponible, proyecta los puntos sobre la calle más cercana.
     * 
     * @param origen Nodo de origen (puede ser Central, Poste o Poste sugerido)
     * @param destino Nodo de destino (generalmente un Cliente)
     * @return Lista de nodos sugeridos (con ID negativo, tipo SUGERIDO)
     */
    public List<Nodo> sugerirPostes(Nodo origen, Nodo destino) {
        List<Nodo> sugeridos = new ArrayList<>();
        
        if (origen == null || destino == null) {
            return sugeridos;
        }
        
        double distancia = origen.distanciaA(destino);
        double distanciaMaxima = obtenerDistanciaMaxima(origen, destino);
        
        if (distancia <= distanciaMaxima) {
            return sugeridos;
        }
        
        double saltoMetros = determinarSaltoOptimo(origen, destino);
        int cantidadPostes = (int) Math.ceil(distancia / saltoMetros) - 1;
        
        if (cantidadPostes > 10) {
            cantidadPostes = 10;
        }
        
        if (cantidadPostes <= 0) {
            return sugeridos;
        }
        
        double incrementoLat = (destino.getLatitud() - origen.getLatitud()) / (cantidadPostes + 1);
        double incrementoLon = (destino.getLongitud() - origen.getLongitud()) / (cantidadPostes + 1);
        
        for (int i = 1; i <= cantidadPostes; i++) {
            int idTemporal = -(10000 + i + (int)(System.currentTimeMillis() % 10000));
            
            double latitudInterpolada = origen.getLatitud() + (incrementoLat * i);
            double longitudInterpolada = origen.getLongitud() + (incrementoLon * i);
            
            double latitudFinal = latitudInterpolada;
            double longitudFinal = longitudInterpolada;
            
            // Si tenemos acceso a la BD, proyectar el punto sobre la calle más cercana
            if (calleRepository != null) {
                try {
                    double[] puntoSobreCalle = proyectarSobreCalleCercana(longitudInterpolada, latitudInterpolada);
                    if (puntoSobreCalle != null) {
                        longitudFinal = puntoSobreCalle[0];
                        latitudFinal = puntoSobreCalle[1];
                        System.out.println("  Sugerido " + i + " proyectado sobre calle: (" + longitudFinal + ", " + latitudFinal + ")");
                    }
                } catch (Exception e) {
                    System.err.println("  Error proyectando sugerido " + i + ": " + e.getMessage());
                }
            }
            
            Nodo sugerido = new Nodo(
                idTemporal,
                "SUGERIDO_" + System.currentTimeMillis() + "_" + i,
                TipoNodo.SUGERIDO,
                CAPACIDAD_SUGERIDO,
                latitudFinal,
                longitudFinal
            );
            
            sugerido.setEstado(EstadoNodo.DISPONIBLE);
            sugerido.setClientesActuales(0);
            sugeridos.add(sugerido);
        }
        
        return sugeridos;
    }
    
    /**
     * Proyecta un punto (longitud, latitud) sobre la calle más cercana usando PostGIS
     * @param longitud Coordenada X
     * @param latitud Coordenada Y
     * @return Arreglo [longitud, latitud] del punto proyectado, o null si no hay calle cercana
     */
    private double[] proyectarSobreCalleCercana(double longitud, double latitud) {
        try {
            // Buscar la calle más cercana al punto interpolado
            String sql = """
                SELECT ST_X(ST_ClosestPoint(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326))) AS lon,
                       ST_Y(ST_ClosestPoint(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326))) AS lat
                FROM calles
                ORDER BY ST_Distance(geom, ST_SetSRID(ST_MakePoint(?, ?), 4326))
                LIMIT 1
                """;
            
            java.sql.Connection conn = com.fibra.backend2.config.DatabaseConnection.getConnection();
            java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDouble(1, longitud);
            stmt.setDouble(2, latitud);
            stmt.setDouble(3, longitud);
            stmt.setDouble(4, latitud);
            stmt.setDouble(5, longitud);
            stmt.setDouble(6, latitud);
            
            java.sql.ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                double lon = rs.getDouble("lon");
                double lat = rs.getDouble("lat");
                rs.close();
                stmt.close();
                conn.close();
                return new double[]{lon, lat};
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (Exception e) {
            System.err.println("Error proyectando punto sobre calle: " + e.getMessage());
        }
        return null;
    }
    
    /**
     * Genera postes sugeridos para conectar una cadena completa desde un origen
     * hasta un destino, pasando por puntos intermedios.
     */
    public List<Nodo> sugerirPostesEncadenados(List<Nodo> nodosExistentes, Nodo destino) {
        List<Nodo> todosSugeridos = new ArrayList<>();
        
        if (nodosExistentes == null || nodosExistentes.isEmpty() || destino == null) {
            return todosSugeridos;
        }
        
        Nodo ultimoNodo = nodosExistentes.get(nodosExistentes.size() - 1);
        double distancia = ultimoNodo.distanciaA(destino);
        
        if (distancia <= DISTANCIA_MAX_POSTE_CLIENTE) {
            return todosSugeridos;
        }
        
        Nodo mejorOrigen = null;
        double mejorDistancia = Double.MAX_VALUE;
        
        for (Nodo nodo : nodosExistentes) {
            double d = nodo.distanciaA(destino);
            if (d < mejorDistancia && d <= DISTANCIA_MAX_POSTE_POSTE) {
                mejorDistancia = d;
                mejorOrigen = nodo;
            }
        }
        
        if (mejorOrigen != null && mejorDistancia <= DISTANCIA_MAX_POSTE_CLIENTE) {
            return todosSugeridos;
        }
        
        Nodo origenParaExpansion = mejorOrigen != null ? mejorOrigen : ultimoNodo;
        
        return sugerirPostes(origenParaExpansion, destino);
    }
    
    private double determinarSaltoOptimo(Nodo origen, Nodo destino) {
        if (destino.getTipo() == TipoNodo.CLIENTE) {
            return DISTANCIA_MAX_POSTE_CLIENTE;
        }
        
        if (esPoste(origen) && esPoste(destino)) {
            return DISTANCIA_MAX_POSTE_POSTE;
        }
        
        if (origen.getTipo() == TipoNodo.CENTRAL && esPoste(destino)) {
            return DISTANCIA_MAX_CENTRAL_POSTE;
        }
        
        return DISTANCIA_MAX_POSTE_CLIENTE;
    }
    
    private double obtenerDistanciaMaxima(Nodo origen, Nodo destino) {
        if (origen == null || destino == null) {
            return 0;
        }
        
        if ((esCliente(origen) && esPoste(destino)) || (esCliente(destino) && esPoste(origen))) {
            return DISTANCIA_MAX_POSTE_CLIENTE;
        }
        
        if ((origen.getTipo() == TipoNodo.CENTRAL && esPoste(destino)) ||
            (destino.getTipo() == TipoNodo.CENTRAL && esPoste(origen))) {
            return DISTANCIA_MAX_CENTRAL_POSTE;
        }
        
        if (esPoste(origen) && esPoste(destino)) {
            return DISTANCIA_MAX_POSTE_POSTE;
        }
        
        return 0;
    }
    
    public boolean necesitaExpansion(Nodo origen, Nodo destino) {
        if (origen == null || destino == null) {
            return false;
        }
        
        double distancia = origen.distanciaA(destino);
        double distanciaMaxima = obtenerDistanciaMaxima(origen, destino);
        
        return distancia > distanciaMaxima;
    }
    
    public int calcularCantidadPostesSugeridos(Nodo origen, Nodo destino) {
        if (!necesitaExpansion(origen, destino)) {
            return 0;
        }
        
        double distancia = origen.distanciaA(destino);
        double saltoMetros = determinarSaltoOptimo(origen, destino);
        
        int cantidad = (int) Math.ceil(distancia / saltoMetros) - 1;
        
        return Math.min(cantidad, 10);
    }
    
    private boolean esPoste(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.POSTE_PRINCIPAL || 
               nodo.getTipo() == TipoNodo.POSTE_SECUNDARIO;
    }
    
    private boolean esCliente(Nodo nodo) {
        return nodo.getTipo() == TipoNodo.CLIENTE;
    }
}