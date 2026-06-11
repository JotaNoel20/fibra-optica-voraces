package com.fibra.backend2.dto;

public class ResultadoDTO<T> {

    private final boolean exitoso;
    private final String mensaje;
    private final T datos;

    public ResultadoDTO(boolean exitoso, String mensaje, T datos) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.datos = datos;
    }

    public boolean isExitoso() {
        return exitoso;
    }

    public String getMensaje() {
        return mensaje;
    }

    public T getDatos() {
        return datos;
    }
}
