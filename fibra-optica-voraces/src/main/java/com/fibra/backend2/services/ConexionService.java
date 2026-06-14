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
        
        

        // Verificar que los nodos existan en la base de datos
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

        // REGLA 1: No conectar nodos INACTIVOS
        if ("INACTIVO".equalsIgnoreCase(origen.getEstado()) || "INACTIVO".equalsIgnoreCase(destino.getEstado())) {
            System.err.println("  ERROR: Nodo INACTIVO detectado - Origen estado: " + origen.getEstado() + " | Destino estado: " + destino.getEstado());
            throw new SpatialException("Operación cancelada: Uno de los nodos seleccionados se encuentra INACTIVO.");
        }

        // REGLA 2: Verificar que no exista ya una conexión entre estos mismos nodos
        List<ConexionDTO> conexionesExistentes = conexionRepository.listarTodas();
        boolean yaExisteConexion = conexionesExistentes.stream()
            .anyMatch(c -> (c.getOrigenId() == conexion.getOrigenId() && c.getDestinoId() == conexion.getDestinoId()) ||
                           (c.getOrigenId() == conexion.getDestinoId() && c.getDestinoId() == conexion.getOrigenId()));
        if (yaExisteConexion) {
            System.err.println("  ERROR: Ya existe una conexión entre estos nodos!");
            throw new SpatialException("Ya existe una conexión entre " + conexion.getOrigenId() + " y " + conexion.getDestinoId());
        }

        // REGLA 3: Un cliente solo puede tener una conexión
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

        // REGLA 4: Control de capacidad del poste cuando se conecta un cliente
        if ("CLIENTE".equalsIgnoreCase(destino.getTipo())) {
            // Verificar que el poste origen no esté saturado
            if (origen.getClientesActuales() >= origen.getCapacidadMax()) {
                System.err.println("  ERROR: Poste origen saturado - Clientes: " + origen.getClientesActuales() + "/" + origen.getCapacidadMax());
                throw new SpatialException("Operación cancelada: El poste origen ha alcanzado su capacidad máxima (" + origen.getCapacidadMax() + " clientes).");
            }
            
            // Incrementar contador de clientes del poste origen
            int nuevosClientes = origen.getClientesActuales() + 1;
            origen.setClientesActuales(nuevosClientes);
            System.out.println("  Poste origen - Clientes actualizados: " + nuevosClientes + "/" + origen.getCapacidadMax());
            
            // Si alcanzó el límite, marcar como SATURADO
            if (nuevosClientes >= origen.getCapacidadMax()) {
                origen.setEstado("SATURADO");
                System.out.println("  Poste origen ahora está SATURADO");
            }
            
            // Guardar cambios del poste
            nodoRepository.actualizar(origen);
            System.out.println("  Poste origen actualizado en BD");
        }
        
        // REGLA 5: Si el origen es un CLIENTE, controlar capacidad del poste destino
        if ("CLIENTE".equalsIgnoreCase(origen.getTipo())) {
            if (destino.getClientesActuales() >= destino.getCapacidadMax()) {
                System.err.println("  ERROR: Poste destino saturado - Clientes: " + destino.getClientesActuales() + "/" + destino.getCapacidadMax());
                throw new SpatialException("Operación cancelada: El poste destino ha alcanzado su capacidad máxima (" + destino.getCapacidadMax() + " clientes).");
            }
            
            int nuevosClientes = destino.getClientesActuales() + 1;
            destino.setClientesActuales(nuevosClientes);
            System.out.println("  Poste destino - Clientes actualizados: " + nuevosClientes + "/" + destino.getCapacidadMax());
            
            if (nuevosClientes >= destino.getCapacidadMax()) {
                destino.setEstado("SATURADO");
                System.out.println("  Poste destino ahora está SATURADO");
            }
            
            nodoRepository.actualizar(destino);
            System.out.println("  Poste destino actualizado en BD");
        }

        System.out.println("  Validación OK. Origen: " + origen.getId() + " (" + origen.getTipo() + "), Destino: " + destino.getId() + " (" + destino.getTipo() + ")");

        // Guardar la conexión
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