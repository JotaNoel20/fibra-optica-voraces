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

    public List<Nodo> obtenerVecinos(Nodo nodo) {

        List<Nodo> vecinos = new ArrayList<>();

        for (Arista arista : aristas) {

            if (arista.getOrigen().equals(nodo)) {
                vecinos.add(arista.getDestino());
            }

            if (arista.getDestino().equals(nodo)) {
                vecinos.add(arista.getOrigen());
            }
        }

        return vecinos;
    }

    public List<Nodo> getNodos() {
        return nodos;
    }

    public List<Arista> getAristas() {
        return aristas;
    }
}