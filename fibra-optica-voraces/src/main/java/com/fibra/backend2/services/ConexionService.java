package com.fibra.backend2.services;

import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;
import com.fibra.backend2.repositories.ConexionRepository;
import com.fibra.backend2.repositories.NodoRepository;

import java.util.List;

public class ConexionService {

    private final ConexionRepository conexionRepository;
    private final NodoRepository nodoRepository; 

    public ConexionService(ConexionRepository conexionRepository, NodoRepository nodoRepository) {
        this.conexionRepository = conexionRepository;
        this.nodoRepository = nodoRepository;
    }

    public ConexionDTO guardarConexion(ConexionDTO conexion) {
        System.out.println("\n=== ConexionService.guardarConexion ===");
        System.out.println("  Recibido - Origen ID: " + conexion.getOrigenId() + " | Destino ID: " + conexion.getDestinoId() + " | Distancia: " + conexion.getDistancia());
        
        if (conexion == null) {
            System.err.println("  ERROR: conexion es null");
            throw new SpatialException("La conexión no puede ser nula.");
        }

        System.out.println("  Verificando existencia de nodos en BD...");
        NodoDTO origen = nodoRepository.buscarPorId(conexion.getOrigenId());
        NodoDTO destino = nodoRepository.buscarPorId(conexion.getDestinoId());

        if (origen == null) {
            System.err.println("  ERROR: Nodo origen ID " + conexion.getOrigenId() + " no existe en BD!");
            throw new SpatialException("El nodo origen ID " + conexion.getOrigenId() + " no existe.");
        }
        if (destino == null) {
            System.err.println("  ERROR: Nodo destino ID " + conexion.getDestinoId() + " no existe en BD!");
            throw new SpatialException("El nodo destino ID " + conexion.getDestinoId() + " no existe.");
        }

        if ("INACTIVO".equalsIgnoreCase(origen.getEstado()) || "INACTIVO".equalsIgnoreCase(destino.getEstado())) {
            System.err.println("  ERROR: Nodo INACTIVO detectado");
            throw new SpatialException("Operación cancelada: Uno de los nodos seleccionados se encuentra INACTIVO.");
        }

        List<ConexionDTO> conexionesExistentes = conexionRepository.listarTodas();
        boolean yaExisteConexion = conexionesExistentes.stream()
            .anyMatch(c -> (c.getOrigenId() == conexion.getOrigenId() && c.getDestinoId() == conexion.getDestinoId()) ||
                           (c.getOrigenId() == conexion.getDestinoId() && c.getDestinoId() == conexion.getOrigenId()));
        if (yaExisteConexion) {
            System.err.println("  ERROR: Ya existe una conexión entre estos nodos!");
            throw new SpatialException("Ya existe una conexión entre " + conexion.getOrigenId() + " y " + conexion.getDestinoId());
        }

        if ("CLIENTE".equalsIgnoreCase(destino.getTipo())) {
            boolean yaTieneConexion = conexionesExistentes.stream()
                .anyMatch(c -> c.getOrigenId() == destino.getId() || c.getDestinoId() == destino.getId());
            if (yaTieneConexion) {
                System.err.println("  ERROR: Cliente destino ya tiene una conexión activa!");
                throw new SpatialException("El cliente ya tiene una conexión activa.");
            }
        }
        
        if ("CLIENTE".equalsIgnoreCase(origen.getTipo())) {
            boolean yaTieneConexion = conexionesExistentes.stream()
                .anyMatch(c -> c.getOrigenId() == origen.getId() || c.getDestinoId() == origen.getId());
            if (yaTieneConexion) {
                System.err.println("  ERROR: Cliente origen ya tiene una conexión activa!");
                throw new SpatialException("El cliente ya tiene una conexión activa.");
            }
        }

        if ("CLIENTE".equalsIgnoreCase(destino.getTipo())) {
            if (origen.getClientesActuales() >= origen.getCapacidadMax()) {
                System.err.println("  ERROR: Poste origen saturado");
                throw new SpatialException("Operación cancelada: El poste origen está SATURADO.");
            }
            
            int nuevosClientesPoste = origen.getClientesActuales() + 1;
            origen.setClientesActuales(nuevosClientesPoste);
            
            if (nuevosClientesPoste >= origen.getCapacidadMax()) {
                origen.setEstado("SATURADO");
            }
            nodoRepository.actualizar(origen);
            
            destino.setClientesActuales(1);
            nodoRepository.actualizar(destino);
        }
        
        if ("CLIENTE".equalsIgnoreCase(origen.getTipo())) {
            if (destino.getClientesActuales() >= destino.getCapacidadMax()) {
                System.err.println("  ERROR: Poste destino saturado");
                throw new SpatialException("Operación cancelada: El poste destino está SATURADO.");
            }
            
            int nuevosClientesPoste = destino.getClientesActuales() + 1;
            destino.setClientesActuales(nuevosClientesPoste);
            
            if (nuevosClientesPoste >= destino.getCapacidadMax()) {
                destino.setEstado("SATURADO");
            }
            nodoRepository.actualizar(destino);
            
            origen.setClientesActuales(1);
            nodoRepository.actualizar(origen);
        }

        System.out.println("  Validación OK. Origen: " + origen.getId() + " (" + origen.getTipo() + "), Destino: " + destino.getId() + " (" + destino.getTipo() + ")");

        ConexionDTO resultado = conexionRepository.guardar(conexion);
        System.out.println("  Conexión guardada exitosamente con ID: " + resultado.getId());
        
        return resultado;
    }

    public List<ConexionDTO> listarConexiones() {
        System.out.println("\n=== ConexionService.listarConexiones ===");
        List<ConexionDTO> conexiones = conexionRepository.listarTodas();
        System.out.println("  Conexiones encontradas: " + conexiones.size());
        return conexiones;
    }

    public void eliminarConexion(int id) {
        System.out.println("\n=== ConexionService.eliminarConexion ===");
        System.out.println("  Eliminando conexión ID: " + id);
        conexionRepository.eliminar(id);
    }
}