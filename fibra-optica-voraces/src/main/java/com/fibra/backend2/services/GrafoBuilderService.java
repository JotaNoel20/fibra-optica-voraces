package com.fibra.backend2.services;

import com.fibra.backend1.graph.Arista;
import com.fibra.backend1.graph.Grafo;
import com.fibra.backend1.graph.Nodo;
import com.fibra.backend2.dto.ConexionDTO;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.exceptions.SpatialException;
import com.fibra.backend2.mapper.NodoMapper;
import com.fibra.backend2.repositories.ConexionRepository;
import com.fibra.backend2.repositories.NodoRepository;

import java.util.HashMap;
import java.util.Map;

public class GrafoBuilderService {

    private final NodoRepository nodoRepository;
    private final ConexionRepository conexionRepository;
    private final NodoMapper nodoMapper;

    public GrafoBuilderService(NodoRepository nodoRepository, ConexionRepository conexionRepository,
                               NodoMapper nodoMapper) {
        this.nodoRepository = nodoRepository;
        this.conexionRepository = conexionRepository;
        this.nodoMapper = nodoMapper;
    }

    public Grafo construirGrafo() {
        Grafo grafo = new Grafo();
        Map<Integer, Nodo> nodosPorId = new HashMap<>();

        for (NodoDTO nodoDTO : nodoRepository.listarTodos()) {
            Nodo nodo = nodoMapper.toModel(nodoDTO);
            grafo.agregarNodo(nodo);
            nodosPorId.put(nodo.getId(), nodo);
        }

        for (ConexionDTO conexionDTO : conexionRepository.listarTodas()) {
            Nodo origen = nodosPorId.get(conexionDTO.getOrigenId());
            Nodo destino = nodosPorId.get(conexionDTO.getDestinoId());

            if (origen == null || destino == null) {
                throw new SpatialException(
                        "La conexion " + conexionDTO.getId() + " referencia nodos inexistentes."
                );
            }
            if (conexionDTO.getDistancia() == null) {
                throw new SpatialException(
                        "La conexion " + conexionDTO.getId() + " no tiene distancia."
                );
            }

            grafo.agregarArista(new Arista(origen, destino, conexionDTO.getDistancia()));
        }

        return grafo;
    }
}
