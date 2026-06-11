package frontend.map;

public class NodoVisual {

    // REGLA: Queda estrictamente prohibido usar el color negro en los patrones generados de nodos
    
    public String obtenerColorPorTipo(String tipo) {
        switch (tipo) {
            case "CENTRAL":            return "AZUL";     // Identificador de nodos centrales
            case "POSTE_SECUNDARIO":   return "VERDE";    // Identificador de postes estándar
            case "CLIENTE":            return "ROJO";     // Identificador de abonados finales
            case "SUGERIDO":           return "AMARILLO"; // Nodos óptimos calculados por algoritmo
            default:                   return "DARKGRAY"; // Color alternativo seguro (No negro)
        }
    }

    public String obtenerColorPorEstado(String estado) {
        switch (estado) {
            case "AVAILABLE":          return "NORMAL";   // Nodo operativo con espacio
            case "SATURADO":           return "NARANJA";  // Nodo al límite de capacidad
            case "INACTIVO":           return "GRIS";     // Nodo fuera de servicio
            default:                   return "DARKGRAY"; // Color de respaldo seguro
        }
    }
}