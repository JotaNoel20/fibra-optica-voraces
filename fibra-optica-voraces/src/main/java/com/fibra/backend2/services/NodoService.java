package com.fibra.backend2.services;

import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.repositories.NodoRepository;

import java.util.List;
import java.util.Optional;

public class NodoService {

    private final NodoRepository nodoRepository;

    public NodoService(NodoRepository nodoRepository) {
        this.nodoRepository = nodoRepository;
    }

    public NodoDTO crearNodo(NodoDTO nodo) {
        return nodoRepository.guardar(nodo);
    }

    public Optional<NodoDTO> obtenerNodo(int id) {
        return nodoRepository.buscarPorId(id);
    }

    public List<NodoDTO> listarNodos() {
        return nodoRepository.listarTodos();
    }

    public boolean eliminarNodo(int id) {
        return nodoRepository.eliminar(id);
    }
}
