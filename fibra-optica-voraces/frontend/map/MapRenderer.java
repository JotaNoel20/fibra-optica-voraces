package frontend.map;

import java.util.List;

public class MapRenderer {

    // Métodos core exigidos en la Sección 8 del manual de desarrollo
    
    public void dibujarCalles(List<Object> listaCalles) {
        System.out.println("MapRenderer: Dibujando " + listaCalles.size() + " segmentos viales en el lienzo...");
        // Bucle para iterar las geometrías y pintar líneas
    }

    public void dibujarNodos(List<Object> listaNodos) {
        System.out.println("MapRenderer: Dibujando " + listaNodos.size() + " puntos de infraestructura (Centrales/Postes)...");
        // Mapea cada elemento llamando a NodoVisual para aplicar su respectivo color
    }

    public void dibujarConexiones(List<Object> listaConexiones) {
        System.out.println("MapRenderer: Dibujando " + listaConexiones.size() + " cables de fibra óptica activos...");
        // Recorre los enlaces y manda a llamar a ConexionVisual
    }
}