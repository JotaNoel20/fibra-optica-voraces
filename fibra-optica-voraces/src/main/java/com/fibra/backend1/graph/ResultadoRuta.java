package com.fibra.backend1.graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ResultadoRuta {

    private List<Arista> conexiones;
    private double costoTotal;
    private double distanciaTotal;
    private int cantidadPostes;
    private List<Nodo> nodosSugeridos;
    private Set<Integer> idsClientesConectados;  // NUEVO: IDs de clientes conectados en esta red

    /**
     * Constructor principal para resultados de algoritmos
     */
    public ResultadoRuta(List<Arista> conexiones, double costoTotal, double distanciaTotal, int cantidadPostes) {
        this.conexiones = conexiones != null ? conexiones : new ArrayList<>();
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.cantidadPostes = cantidadPostes;
        this.nodosSugeridos = new ArrayList<>();
        this.idsClientesConectados = new HashSet<>();
    }

    /**
     * Constructor con nodos sugeridos (para expansión de red)
     */
    public ResultadoRuta(List<Arista> conexiones, double costoTotal, double distanciaTotal, 
                         int cantidadPostes, List<Nodo> nodosSugeridos) {
        this.conexiones = conexiones != null ? conexiones : new ArrayList<>();
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.cantidadPostes = cantidadPostes;
        this.nodosSugeridos = nodosSugeridos != null ? nodosSugeridos : new ArrayList<>();
        this.idsClientesConectados = new HashSet<>();
    }

    // ========== GETTERS Y SETTERS ==========

    public List<Arista> getConexiones() {
        return conexiones;
    }

    public void setConexiones(List<Arista> conexiones) {
        this.conexiones = conexiones != null ? conexiones : new ArrayList<>();
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public void setCostoTotal(double costoTotal) {
        this.costoTotal = costoTotal;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public void setDistanciaTotal(double distanciaTotal) {
        this.distanciaTotal = distanciaTotal;
    }

    public int getCantidadPostes() {
        return cantidadPostes;
    }

    public void setCantidadPostes(int cantidadPostes) {
        this.cantidadPostes = cantidadPostes;
    }

    /**
     * Obtiene la lista de nodos sugeridos (postes propuestos por el algoritmo)
     * Estos nodos NO deben guardarse en la base de datos automáticamente.
     */
    public List<Nodo> getNodosSugeridos() {
        return nodosSugeridos;
    }

    /**
     * Establece la lista de nodos sugeridos
     * @param nodosSugeridos Lista de nodos con tipo SUGERIDO (IDs negativos)
     */
    public void setNodosSugeridos(List<Nodo> nodosSugeridos) {
        this.nodosSugeridos = nodosSugeridos != null ? nodosSugeridos : new ArrayList<>();
    }

    /**
     * Agrega un nodo sugerido a la lista
     */
    public void agregarNodoSugerido(Nodo nodo) {
        if (nodo != null && this.nodosSugeridos != null) {
            this.nodosSugeridos.add(nodo);
        }
    }

    /**
     * Verifica si hay nodos sugeridos
     */
    public boolean hayNodosSugeridos() {
        return nodosSugeridos != null && !nodosSugeridos.isEmpty();
    }

    /**
     * Limpia la lista de nodos sugeridos
     */
    public void limpiarNodosSugeridos() {
        if (nodosSugeridos != null) {
            nodosSugeridos.clear();
        }
    }

    /**
     * Obtiene la cantidad de nodos sugeridos
     */
    public int getCantidadNodosSugeridos() {
        return nodosSugeridos != null ? nodosSugeridos.size() : 0;
    }

    /**
     * Obtiene los IDs de los clientes conectados en esta red
     */
    public Set<Integer> getIdsClientesConectados() {
        return idsClientesConectados;
    }

    /**
     * Establece los IDs de los clientes conectados
     */
    public void setIdsClientesConectados(Set<Integer> idsClientesConectados) {
        this.idsClientesConectados = idsClientesConectados != null ? idsClientesConectados : new HashSet<>();
    }

    /**
     * Agrega un ID de cliente conectado
     */
    public void agregarClienteConectado(int idCliente) {
        if (this.idsClientesConectados != null) {
            this.idsClientesConectados.add(idCliente);
        }
    }

    /**
     * Verifica si hay clientes conectados
     */
    public boolean hayClientesConectados() {
        return idsClientesConectados != null && !idsClientesConectados.isEmpty();
    }

    /**
     * Obtiene la cantidad de clientes conectados
     */
    public int getCantidadClientesConectados() {
        return idsClientesConectados != null ? idsClientesConectados.size() : 0;
    }

    @Override
    public String toString() {
        return "ResultadoRuta{" +
                "conexiones=" + (conexiones != null ? conexiones.size() : 0) +
                ", costoTotal=" + costoTotal +
                ", distanciaTotal=" + distanciaTotal +
                ", cantidadPostes=" + cantidadPostes +
                ", nodosSugeridos=" + (nodosSugeridos != null ? nodosSugeridos.size() : 0) +
                ", clientesConectados=" + (idsClientesConectados != null ? idsClientesConectados.size() : 0) +
                '}';
    }
}