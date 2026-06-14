package com.fibra.backend1.graph;

import com.fibra.backend1.enums.EstadoNodo;
import java.util.ArrayList;
import java.util.List;

public class Grafo {

    private List<Nodo> nodos;
    private List<Arista> aristas;

    public Grafo() {
        nodos = new ArrayList<>();
        aristas = new ArrayList<>();
    }

    public void agregarNodo(Nodo nodo) {
        nodos.add(nodo);
    }

    public void agregarArista(Arista arista) {
        aristas.add(arista);
    }

    /**
     * MEJORA: Filtra y descarta automáticamente vecinos que estén INACTIVOS.
     * Evita que los algoritmos de exploración de rutas consideren postes fuera de servicio.
     */
    public List<Nodo> obtenerVecinos(Nodo nodo) {
        List<Nodo> vecinos = new ArrayList<>();

        for (Arista arista : aristas) {
            if (arista.getOrigen().equals(nodo)) {
                Nodo vecino = arista.getDestino();
                if (vecino.getEstado() != EstadoNodo.INACTIVO) {
                    vecinos.add(vecino);
                }
            }

            if (arista.getDestino().equals(nodo)) {
                Nodo vecino = arista.getOrigen();
                if (vecino.getEstado() != EstadoNodo.INACTIVO) {
                    vecinos.add(vecino);
                }
            }
        }

        return vecinos;
    }

    public List<Nodo> getNodos() { return nodos; }
    public List<Arista> getAristas() { return aristas; }
}