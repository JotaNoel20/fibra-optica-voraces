package com.fibra.backend2.dto;

public class ResultadoDTO {

    private boolean exito;
    private String mensaje;
    private Object data;

    public ResultadoDTO() {
    }

    public ResultadoDTO(boolean exito, String mensaje, Object data) {
        this.exito = exito;
        this.mensaje = mensaje;
        this.data = data;
    }

    public boolean isExito() {
        return exito;
    }

    public void setExito(boolean exito) {
        this.exito = exito;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ResultadoDTO{" +
                "exito=" + exito +
                ", mensaje='" + mensaje + '\'' +
                ", data=" + data +
                '}';
    }
}
