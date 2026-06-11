package com.fibra.backend2.mapper;

import com.fibra.backend1.enums.EstadoNodo;
import com.fibra.backend1.enums.TipoNodo;
import com.fibra.backend1.graph.Nodo;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;

public class NodoMapper {

    public Nodo toModel(NodoDTO dto) {
        if (dto == null) {
            throw new SpatialException("No se puede convertir un NodoDTO nulo.");
        }

        Nodo nodo = new Nodo(
                valorRequerido(dto.getId(), "id"),
                dto.getNombre(),
                mapearTipo(dto.getTipo()),
                valorRequerido(dto.getCapacidadMax(), "capacidadMax"),
                dto.getLatitud(),
                dto.getLongitud()
        );

        int clientesActuales = dto.getClientesActuales() == null ? 0 : dto.getClientesActuales();
        for (int i = 0; i < clientesActuales && nodo.agregarCliente(); i++) {
            // La API real de Backend 1 actualiza clientes mediante agregarCliente().
        }

        if (dto.getEstado() != null && !dto.getEstado().isBlank()) {
            nodo.setEstado(mapearEstado(dto.getEstado()));
        }

        return nodo;
    }

    public NodoDTO toDTO(Nodo nodo) {
        if (nodo == null) {
            throw new SpatialException("No se puede convertir un Nodo nulo.");
        }

        return new NodoDTO(
                nodo.getId(),
                nodo.getNombre(),
                nodo.getTipo().name(),
                nodo.getEstado().name(),
                nodo.getCapacidadMax(),
                nodo.getClientesActuales(),
                nodo.getLatitud(),
                nodo.getLongitud()
        );
    }

    private TipoNodo mapearTipo(String tipo) {
        if (tipo == null || tipo.isBlank()) {
            throw new SpatialException("El tipo del nodo es obligatorio.");
        }

        String tipoNormalizado = tipo.trim().toUpperCase();
        if ("POSTE".equals(tipoNormalizado)) {
            return TipoNodo.POSTE_PRINCIPAL;
        }

        try {
            return TipoNodo.valueOf(tipoNormalizado);
        } catch (IllegalArgumentException e) {
            throw new SpatialException("Tipo de nodo no soportado: " + tipo, e);
        }
    }

    private EstadoNodo mapearEstado(String estado) {
        try {
            return EstadoNodo.valueOf(estado.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new SpatialException("Estado de nodo no soportado: " + estado, e);
        }
    }

    private int valorRequerido(Integer valor, String campo) {
        if (valor == null) {
            throw new SpatialException("El campo " + campo + " es obligatorio para construir el grafo.");
        }
        return valor;
    }
}
