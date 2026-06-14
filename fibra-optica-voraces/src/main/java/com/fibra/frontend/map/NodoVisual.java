package com.fibra.frontend.map;

import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import com.fibra.backend2.dto.NodoDTO;

public class NodoVisual extends Circle {

    private final NodoDTO nodoData;

    public NodoVisual(NodoDTO nodoData) {
        this.nodoData = nodoData;
        this.setStroke(Color.WHITE);
        actualizarEstiloVisual();
    }

    /**
     * Ajusta el grosor del borde según el radio actual del nodo
     * Borde = 5% del radio (mínimo 0.1 para nodos muy pequeños)
     */
    public void ajustarBorde() {
        double radio = this.getRadius();
        double strokeWidth = Math.max(0.1, radio * 0.05);  // 5% del radio, mínimo 0.1
        this.setStrokeWidth(strokeWidth);
    }

    /**
     * Actualiza el color y el borde según el tipo y estado del nodo
     */
    public void actualizarEstiloVisual() {
        String tipo = nodoData.getTipo() != null ? nodoData.getTipo().toUpperCase() : "";
        String estado = nodoData.getEstado() != null ? nodoData.getEstado().toUpperCase() : "";

        this.getStrokeDashArray().clear();

        // Nodo SUGERIDO (si se usa)
        if ("SUGERIDO".equals(tipo)) {
            this.setFill(Color.GOLDENROD);
            this.getStrokeDashArray().addAll(3.0, 3.0);
            this.setStroke(Color.GOLD);
            ajustarBorde();
            return;
        }

        // Nodo INACTIVO
        if ("INACTIVO".equals(estado)) {
            this.setFill(Color.CHOCOLATE);
            this.setStroke(Color.WHITE);
            ajustarBorde();
            return;
        }
        
        // Nodo SATURADO
        if ("SATURADO".equals(estado)) {
            this.setFill(Color.PURPLE);
            this.setStroke(Color.WHITE);
            ajustarBorde();
            return;
        }

        // Colores por tipo
        switch (tipo) {
            case "CENTRAL":
                this.setFill(Color.RED);
                this.setStroke(Color.WHITE);
                break;
            case "POSTE_PRINCIPAL":
                this.setFill(Color.ORANGE);
                this.setStroke(Color.WHITE);
                break;
            case "POSTE_SECUNDARIO":
                this.setFill(Color.YELLOW);
                this.setStroke(Color.BLACK);  // Borde negro para postes amarillos
                break;
            case "CLIENTE":
                this.setFill(Color.LIMEGREEN);
                this.setStroke(Color.WHITE);
                break;
            default:
                this.setFill(Color.GRAY);
                this.setStroke(Color.WHITE);
                break;
        }
        
        ajustarBorde();
    }

    public NodoDTO getNodoData() {
        return nodoData;
    }
}