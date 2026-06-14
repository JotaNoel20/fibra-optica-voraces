package com.fibra.backend2.services;

import com.fibra.backend2.repositories.CalleRepository;

public class SpatialValidationService {

    // NORMAS TÉCNICAS URBANAS EN METROS
    // Aumentadas para mejor experiencia de usuario
    private static final double DISTANCIA_MAX_POSTE_ACERA = 8.0;  // Aumentado de 5.0 a 8.0 metros
    private static final double DISTANCIA_MIN_CLIENTE_MANZANO = 3.0; // Reducido de 5.0 a 3.0 metros
    private static final double DISTANCIA_MAX_CENTRAL = 20.0; // Central hasta 20m de una calle

    private final CalleRepository calleRepository;

    public SpatialValidationService(CalleRepository calleRepository) {
        this.calleRepository = calleRepository;
    }

    /**
     * Aplica reglas topológicas espaciales diferenciadas según el tipo de infraestructura física.
     * Cruza los datos con las funciones analíticas espaciales de PostGIS.
     * 
     * @param tipo Tipo de nodo ('CENTRAL', 'POSTE_PRINCIPAL', 'POSTE_SECUNDARIO', 'CLIENTE')
     * @param latitud Coordenada Y
     * @param longitud Coordenada X
     * @return true si la ubicación es urbanísticamente válida para la red de fibra
     */
    public boolean validarUbicacion(String tipo, double latitud, double longitud) {
        if (tipo == null || tipo.isEmpty()) {
            return false;
        }

        String tipoUpper = tipo.toUpperCase();
        double distancia = distanciaACalle(latitud, longitud);
        
        System.out.println("=== VALIDACIÓN ESPACIAL ===");
        System.out.println("  Tipo: " + tipoUpper);
        System.out.println("  Distancia a calle más cercana: " + String.format("%.2f", distancia) + "m");

        switch (tipoUpper) {
            case "CENTRAL":
                // La central debe estar cerca de una calle principal
                boolean centralValida = distancia <= DISTANCIA_MAX_CENTRAL;
                System.out.println("  CENTRAL - Válida si distancia ≤ " + DISTANCIA_MAX_CENTRAL + "m: " + centralValida);
                return centralValida;

            case "POSTE_PRINCIPAL":
            case "POSTE_SECUNDARIO":
                // El poste debe estar plantado sobre la acera (cerca del eje de la calle)
                boolean posteValido = distancia <= DISTANCIA_MAX_POSTE_ACERA;
                System.out.println("  POSTE - Válido si distancia ≤ " + DISTANCIA_MAX_POSTE_ACERA + "m: " + posteValido);
                return posteValido;

            case "CLIENTE":
                // El cliente (casa) debe estar dentro del manzano (no sobre la calle)
                boolean clienteValido = distancia >= DISTANCIA_MIN_CLIENTE_MANZANO;
                System.out.println("  CLIENTE - Válido si distancia ≥ " + DISTANCIA_MIN_CLIENTE_MANZANO + "m: " + clienteValido);
                return clienteValido;

            default:
                System.out.println("  Tipo desconocido: " + tipoUpper + " - INVÁLIDO");
                return false;
        }
    }

    /**
     * Valida específicamente un poste (más permisivo)
     */
    public boolean validarPoste(double latitud, double longitud) {
        return validarUbicacion("POSTE_SECUNDARIO", latitud, longitud);
    }

    /**
     * Valida específicamente un cliente
     */
    public boolean validarCliente(double latitud, double longitud) {
        return validarUbicacion("CLIENTE", latitud, longitud);
    }

    /**
     * Devuelve la distancia ortogonal mínima en metros desde el punto hacia el eje de calle más cercano.
     */
    public double distanciaACalle(double latitud, double longitud) {
        return calleRepository.distanciaMinimaACalle(latitud, longitud);
    }

    /**
     * Verifica si existe una calle cercana al punto
     */
    public boolean existeCalleCercana(double latitud, double longitud, double toleranciaMetros) {
        return calleRepository.existeCalleCercana(latitud, longitud, toleranciaMetros);
    }
}