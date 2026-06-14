package com.fibra.backend2.services;

import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;
import com.fibra.backend2.repositories.NodoRepository;
import com.fibra.backend2.repositories.ConexionRepository;

import java.util.List;

public class NodoService {

    private final NodoRepository nodoRepository;
    private final SpatialValidationService spatialValidationService;
    private ConexionRepository conexionRepository;

    public NodoService(NodoRepository nodoRepository, SpatialValidationService spatialValidationService) {
        this.nodoRepository = nodoRepository;
        this.spatialValidationService = spatialValidationService;
    }
    
    public NodoService(NodoRepository nodoRepository, SpatialValidationService spatialValidationService,
                       ConexionRepository conexionRepository) {
        this.nodoRepository = nodoRepository;
        this.spatialValidationService = spatialValidationService;
        this.conexionRepository = conexionRepository;
    }

    public NodoDTO crearNodo(NodoDTO nodo) {
        if (nodo == null) {
            throw new SpatialException("El nodo no puede ser nulo.");
        }

        if ("POSTE".equalsIgnoreCase(nodo.getTipo())) {
            nodo.setTipo("POSTE_PRINCIPAL");
        }

        if (nodo.getEstado() == null || nodo.getEstado().isEmpty()) {
            nodo.setEstado("DISPONIBLE");
        }

        // VALIDACIÓN ANTI-DUPLICADOS: Verificar si ya existe un nodo del mismo tipo en la misma ubicación
        List<NodoDTO> existentes = nodoRepository.listarTodos();
        boolean existe = existentes.stream()
            .anyMatch(n -> n.getTipo().equalsIgnoreCase(nodo.getTipo())
                && Math.abs(n.getLatitud() - nodo.getLatitud()) < 0.00001
                && Math.abs(n.getLongitud() - nodo.getLongitud()) < 0.00001);
        
        if (existe) {
            throw new SpatialException("Ya existe un nodo de tipo " + nodo.getTipo() + " en esta ubicación.");
        }

        if (!spatialValidationService.validarUbicacion(nodo.getTipo(), nodo.getLatitud(), nodo.getLongitud())) {
            throw new SpatialException("Ubicación geográfica inválida para el tipo de nodo: " + nodo.getTipo());
        }

        return nodoRepository.guardar(nodo);
    }

    public NodoDTO actualizarEstadoNodo(int id, String nuevoEstado) {
        NodoDTO nodo = nodoRepository.buscarPorId(id);
        if (nodo == null) {
            throw new SpatialException("El nodo con ID " + id + " no existe.");
        }
        nodo.setEstado(nuevoEstado.toUpperCase());
        // Usar ACTUALIZAR en lugar de GUARDAR (que hace INSERT)
        return nodoRepository.actualizar(nodo);
    }

    public NodoDTO obtenerNodo(int id) {
        return nodoRepository.buscarPorId(id);
    }

    public List<NodoDTO> listarNodos() {
        return nodoRepository.listarTodos();
    }

    public void eliminarNodo(int id) {
        if (conexionRepository != null) {
            conexionRepository.eliminarPorNodo(id);
        }
        nodoRepository.eliminar(id);
        System.out.println("Eliminando físicamente nodo ID: " + id);
    }
    
    public void eliminarTodosNodos() {
        List<NodoDTO> nodos = nodoRepository.listarTodos();
        for (NodoDTO nodo : nodos) {
            if (conexionRepository != null) {
                conexionRepository.eliminarPorNodo(nodo.getId());
            }
            nodoRepository.eliminar(nodo.getId());
        }
    }
    
    public int contarNodos() {
        return nodoRepository.listarTodos().size();
    }
}