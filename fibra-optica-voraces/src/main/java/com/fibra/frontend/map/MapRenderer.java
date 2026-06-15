package com.fibra.frontend.map;

import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.paint.Color;
import javafx.scene.Group;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fibra.backend2.dto.CalleDTO;
import com.fibra.backend2.dto.NodoDTO;
import com.fibra.backend2.dto.ConexionDTO;

public class MapRenderer extends Pane {

    private double minX = Double.MAX_VALUE;
    private double maxX = -Double.MAX_VALUE;
    private double minY = Double.MAX_VALUE;
    private double maxY = -Double.MAX_VALUE;

    private double widthPx = 1000; 
    private double heightPx = 700;  
    private final double padding = 40;  

    private final Group contenedorLienzo = new Group();
    private final Scale zoomTransform = new Scale(1.0, 1.0);
    private final Translate panTransform = new Translate(0.0, 0.0);

    private final Group capaCalles = new Group();
    private final Group capaConexiones = new Group();
    private final Group capaNodos = new Group();

    private final List<Line> lineasCallesInstanciadas = new ArrayList<>();
    private final double GROSOR_BASE_CALLE = 1.5;
    private final double GROSOR_MINIMO_CALLE = 0.8;
    
    // Tamaño base del nodo (se aplica zoom invertido)
    private final double TAMANO_BASE_NODO = 5.0;
    private final double TAMANO_MAXIMO_NODO = 10.0;
    private final double TAMANO_MINIMO_NODO = 0.2;  // Aumentado de 0.2 a 1.5

    private Map<Integer, NodoDTO> mapaNodosCache = new HashMap<>();

    public MapRenderer() {

        this.setStyle("-fx-background-color: #1E1E1E;"); 
        this.setPrefSize(widthPx, heightPx);
        
        contenedorLienzo.getChildren().addAll(capaCalles, capaConexiones, capaNodos);
        this.getChildren().add(contenedorLienzo);
        
        zoomTransform.setPivotX(0);
        zoomTransform.setPivotY(0);
        
        contenedorLienzo.getTransforms().addAll(panTransform, zoomTransform);
        this.setFocusTraversable(true);
        inicializarControlesNavegacion();
    }

    private void inicializarControlesNavegacion() {
        this.setOnScroll(event -> {
            event.consume();
            if (event.getDeltaY() == 0) return;

            double factor = (event.getDeltaY() > 0) ? 1.15 : 0.85;
            double viejoZoom = zoomTransform.getX();
            double nuevoZoom = viejoZoom * factor;

            if (nuevoZoom < 0.4 || nuevoZoom > 40.0) return;

            double mouseX = event.getX();
            double mouseY = event.getY();
            double mapaX = (mouseX - panTransform.getX()) / viejoZoom;
            double mapaY = (mouseY - panTransform.getY()) / viejoZoom;

            zoomTransform.setX(nuevoZoom);
            zoomTransform.setY(nuevoZoom);
            panTransform.setX(mouseX - (mapaX * nuevoZoom));
            panTransform.setY(mouseY - (mapaY * nuevoZoom));

            double nuevoGrosor = Math.max(GROSOR_MINIMO_CALLE, Math.min(GROSOR_BASE_CALLE, GROSOR_BASE_CALLE / nuevoZoom));
            for (Line linea : lineasCallesInstanciadas) {
                linea.setStrokeWidth(nuevoGrosor);
            }
            
            redibujarNodos();
        });

        final double[] anchor = new double[2];
        this.setOnMousePressed(event -> {
            if (event.isMiddleButtonDown()) {
                anchor[0] = event.getX();
                anchor[1] = event.getY();
            }
        });

        this.setOnMouseDragged(event -> {
            if (event.isMiddleButtonDown()) {
                panTransform.setX(panTransform.getX() + (event.getX() - anchor[0]));
                panTransform.setY(panTransform.getY() + (event.getY() - anchor[1]));
                anchor[0] = event.getX();
                anchor[1] = event.getY();
            }
        });
    }
    
    /**
     * Redibuja los nodos con ZOOM INVERTIDO:
     * - Zoom alejado (factor bajo) → nodos GRANDES
     * - Zoom cerca (factor alto) → nodos PEQUEÑOS
     */
    private void redibujarNodos() {
        if (mapaNodosCache.isEmpty()) return;
        
        capaNodos.getChildren().clear();
        
        double factorZoom = zoomTransform.getX();
        
        // ZOOM INVERTIDO: tamaño = base / factorZoom
        double tamanoVisual = TAMANO_BASE_NODO / factorZoom;
        
        // Aplicar límites
        tamanoVisual = Math.max(TAMANO_MINIMO_NODO, Math.min(TAMANO_MAXIMO_NODO, tamanoVisual));
        
        for (NodoDTO nodo : mapaNodosCache.values()) {
            NodoVisual v = new NodoVisual(nodo);
            v.setCenterX(transformarX(nodo.getLongitud()));
            v.setCenterY(transformarY(nodo.getLatitud()));
            v.setRadius(tamanoVisual);
            v.ajustarBorde();  // El borde se calcula automáticamente según el radio
            capaNodos.getChildren().add(v);
        }
    }

    public void dibujarCalles(List<CalleDTO> calles) {
        if (calles == null || calles.isEmpty()) return;
        calcularLimites(calles);
        Pattern linePattern = Pattern.compile("(-?\\d+\\.\\d+)\\s+(-?\\d+\\.\\d+)");

        for (CalleDTO calle : calles) {
            if (calle.getGeometriaWkt() == null) continue;
            Matcher matcher = linePattern.matcher(calle.getGeometriaWkt());
            List<double[]> puntos = new ArrayList<>();
            while (matcher.find()) {
                puntos.add(new double[]{transformarX(Double.parseDouble(matcher.group(1))), 
                                        transformarY(Double.parseDouble(matcher.group(2)))});
            }
            for (int i = 0; i < puntos.size() - 1; i++) {
                Line linea = new Line(puntos.get(i)[0], puntos.get(i)[1], puntos.get(i+1)[0], puntos.get(i+1)[1]);
                linea.setStroke(Color.web("#555555"));
                linea.setStrokeWidth(GROSOR_BASE_CALLE);
                lineasCallesInstanciadas.add(linea);
                capaCalles.getChildren().add(linea);
            }
        }
    }

    public void dibujarNodos(List<NodoDTO> nodos) {
        if (nodos == null || minX == Double.MAX_VALUE) return;
        
        this.mapaNodosCache = nodos.stream().collect(Collectors.toMap(NodoDTO::getId, n -> n));
        redibujarNodos();
    }

    public void dibujarConexiones(List<ConexionDTO> conexiones, List<NodoDTO> nodos) {
        if (conexiones == null || mapaNodosCache.isEmpty()) return;

        for (ConexionDTO con : conexiones) {
            NodoDTO o = mapaNodosCache.get(con.getOrigenId());
            NodoDTO d = mapaNodosCache.get(con.getDestinoId());

            if (o != null && d != null) {
                Line l = new Line(transformarX(o.getLongitud()), transformarY(o.getLatitud()),
                                  transformarX(d.getLongitud()), transformarY(d.getLatitud()));
                l.setStroke(Color.CYAN);
                l.setStrokeWidth(0.1);  // Grosor reducido para el cable
                capaConexiones.getChildren().add(l);
            }
        }
    }

    public void limpiarNodosYConexiones() {
        capaConexiones.getChildren().clear();
        capaNodos.getChildren().clear();
    }

    public void limpiarMapa() {
        capaCalles.getChildren().clear();
        capaConexiones.getChildren().clear();
        capaNodos.getChildren().clear();
        lineasCallesInstanciadas.clear();
        mapaNodosCache.clear();
    }

    private void calcularLimites(List<CalleDTO> calles) {
        Pattern p = Pattern.compile("-?\\d+\\.\\d+");
        for (CalleDTO c : calles) {
            if (c.getGeometriaWkt() == null) continue;
            Matcher m = p.matcher(c.getGeometriaWkt());
            boolean esX = true;
            while (m.find()) {
                double v = Double.parseDouble(m.group());
                if (esX) { 
                    minX = Math.min(minX, v); 
                    maxX = Math.max(maxX, v); 
                } else { 
                    minY = Math.min(minY, v); 
                    maxY = Math.max(maxY, v); 
                }
                esX = !esX;
            }
        }
    }

    private double transformarX(double x) { 
        return padding + (x - minX) * ((widthPx - 2 * padding) / (maxX - minX)); 
    }
    
    private double transformarY(double y) { 
        return heightPx - (padding + (y - minY) * ((heightPx - 2 * padding) / (maxY - minY))); 
    }
    
    public double revertirX(double pxX) { 
        return ((pxX - padding) / ((widthPx - 2 * padding) / (maxX - minX))) + minX; 
    }
    
    public double revertirY(double pxY) { 
        return ((heightPx - pxY - padding) / ((heightPx - 2 * padding) / (maxY - minY))) + minY; 
    }

    public Group getContenedorLienzo() { 
        return contenedorLienzo; 
    }
    
    public Translate getPanTransform() { 
        return panTransform; 
    }
    
    public Scale getZoomTransform() {
        return zoomTransform;
    }
    
    public void actualizarDimensiones(double w, double h) { 
        this.widthPx = w; 
        this.heightPx = h; 
    }
}