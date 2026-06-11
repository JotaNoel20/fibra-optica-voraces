package com.fibra.backend2.services;

import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;
import com.fibra.backend2.repositories.NodoRepository;

import java.util.List;

public class NodoService {

    private final NodoRepository nodoRepository;
    private final SpatialValidationService spatialValidationService;

    public NodoService(NodoRepository nodoRepository, SpatialValidationService spatialValidationService) {
        this.nodoRepository = nodoRepository;
        this.spatialValidationService = spatialValidationService;
    }

    public NodoDTO crearNodo(NodoDTO nodo) {
        if (nodo == null) {
            throw new SpatialException("El nodo no puede ser nulo.");
        }

        if ("POSTE".equalsIgnoreCase(nodo.getTipo())
                && !spatialValidationService.validarPoste(nodo.getLatitud(), nodo.getLongitud())) {
            throw new SpatialException("No existe una calle cercana para ubicar el poste.");
        }

        return nodoRepository.guardar(nodo);
    }

    public NodoDTO obtenerNodo(int id) {
        return nodoRepository.buscarPorId(id);
    }

    public List<NodoDTO> listarNodos() {
        return nodoRepository.listarTodos();
    }

    public void eliminarNodo(int id) {
        nodoRepository.eliminar(id);
    }
}
