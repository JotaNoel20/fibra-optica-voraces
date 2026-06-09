import java.util.*;

public class PrimAlgorithm {

    public ResultadoRuta generarRed(
            Grafo grafo,
            Nodo central
    ) {

        List<Arista> resultado =
                new ArrayList<>();

        Set<Nodo> visitados =
                new HashSet<>();

        visitados.add(central);

        double costoTotal = 0;
        double distanciaTotal = 0;

        while (
                visitados.size()
                        < grafo.getNodos().size()
        ) {

            Arista mejor = null;

            for (Arista arista : grafo.getAristas()) {

                boolean origen =
                        visitados.contains(
                                arista.getOrigen()
                        );

                boolean destino =
                        visitados.contains(
                                arista.getDestino()
                        );

                if (origen && !destino ||
                        !origen && destino) {

                    if (
                            mejor == null
                                    ||
                                    arista.getCosto()
                                    < mejor.getCosto()
                    ) {
                        mejor = arista;
                    }
                }
            }

            if (mejor == null) {
                break;
            }

            resultado.add(mejor);

            visitados.add(
                    mejor.getOrigen()
            );

            visitados.add(
                    mejor.getDestino()
            );

            costoTotal +=
                    mejor.getCosto();

            distanciaTotal +=
                    mejor.getDistancia();
        }

        return new ResultadoRuta(
                resultado,
                costoTotal,
                distanciaTotal,
                visitados.size()
        );
    }
}