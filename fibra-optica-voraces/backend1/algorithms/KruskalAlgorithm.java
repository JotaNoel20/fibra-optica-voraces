import java.util.*;

public class KruskalAlgorithm {

    public ResultadoRuta generarRed(
            Grafo grafo
    ) {

        List<Arista> resultado =
                new ArrayList<>();

        List<Arista> aristas =
                new ArrayList<>(
                        grafo.getAristas()
                );

        aristas.sort(
                Comparator.comparingDouble(
                        Arista::getCosto
                )
        );

        double costoTotal = 0;
        double distanciaTotal = 0;

        for (Arista arista : aristas) {

            resultado.add(arista);

            costoTotal +=
                    arista.getCosto();

            distanciaTotal +=
                    arista.getDistancia();
        }

        return new ResultadoRuta(
                resultado,
                costoTotal,
                distanciaTotal,
                resultado.size()
        );
    }
}