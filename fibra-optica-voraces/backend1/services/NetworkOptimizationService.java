import java.util.HashMap;
import java.util.Map;

public class NetworkOptimizationService {

    private PrimAlgorithm prim =
            new PrimAlgorithm();

    private KruskalAlgorithm kruskal =
            new KruskalAlgorithm();

    public ResultadoRuta generarRed(
            Grafo grafo,
            Nodo central,
            String algoritmo
    ) {

        if (algoritmo.equalsIgnoreCase("PRIM")) {

            return prim.generarRed(
                    grafo,
                    central
            );
        }

        if (algoritmo.equalsIgnoreCase("KRUSKAL")) {

            return kruskal.generarRed(
                    grafo
            );
        }

        throw new IllegalArgumentException(
                "Algoritmo no soportado"
        );
    }

    public Map<String, ResultadoRuta>
    compararAlgoritmos(
            Grafo grafo,
            Nodo central
    ) {

        Map<String, ResultadoRuta>
                resultados =
                new HashMap<>();

        resultados.put(
                "PRIM",
                prim.generarRed(
                        grafo,
                        central
                )
        );

        resultados.put(
                "KRUSKAL",
                kruskal.generarRed(
                        grafo
                )
        );

        return resultados;
    }
}