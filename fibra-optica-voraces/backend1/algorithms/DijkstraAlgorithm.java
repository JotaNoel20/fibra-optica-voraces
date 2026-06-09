import java.util.*;

public class DijkstraAlgorithm {

    public ResultadoRuta calcularRuta(
            Grafo grafo,
            Nodo origen,
            Nodo destino
    ) {

        Map<Nodo, Double> distancias = new HashMap<>();
        Map<Nodo, Nodo> anteriores = new HashMap<>();

        for (Nodo nodo : grafo.getNodos()) {
            distancias.put(nodo, Double.MAX_VALUE);
        }

        distancias.put(origen, 0.0);

        PriorityQueue<Nodo> cola =
                new PriorityQueue<>(
                        Comparator.comparingDouble(distancias::get)
                );

        cola.add(origen);

        while (!cola.isEmpty()) {

            Nodo actual = cola.poll();

            if (actual.equals(destino)) {
                break;
            }

            for (Arista arista : grafo.getAristas()) {

                Nodo vecino = null;

                if (arista.getOrigen().equals(actual)) {
                    vecino = arista.getDestino();
                }

                if (arista.getDestino().equals(actual)) {
                    vecino = arista.getOrigen();
                }

                if (vecino == null) {
                    continue;
                }

                double nuevaDistancia =
                        distancias.get(actual)
                                + arista.getCosto();

                if (nuevaDistancia < distancias.get(vecino)) {

                    distancias.put(
                            vecino,
                            nuevaDistancia
                    );

                    anteriores.put(
                            vecino,
                            actual
                    );

                    cola.add(vecino);
                }
            }
        }

        List<Arista> ruta = new ArrayList<>();

        double costoTotal =
                distancias.get(destino);

        return new ResultadoRuta(
                ruta,
                costoTotal,
                costoTotal,
                ruta.size()
        );
    }
}