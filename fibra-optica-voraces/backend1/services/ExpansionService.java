import java.util.ArrayList;
import java.util.List;

public class ExpansionService {

    public List<Nodo> sugerirPostes(
            Nodo origen,
            Nodo cliente
    ) {

        List<Nodo> sugeridos =
                new ArrayList<>();

        double distancia =
                origen.distanciaA(cliente);

        if (distancia <= 40) {
            return sugeridos;
        }

        int cantidad =
                (int) Math.ceil(distancia / 40.0) - 1;

        double incrementoLat =
                (cliente.getLatitud() - origen.getLatitud())
                        / (cantidad + 1);

        double incrementoLon =
                (cliente.getLongitud() - origen.getLongitud())
                        / (cantidad + 1);

        for (int i = 1; i <= cantidad; i++) {

            Nodo sugerido =
                    new Nodo(
                            10000 + i,
                            "POSTE_SUGERIDO_" + i,
                            TipoNodo.SUGERIDO,
                            50,
                            origen.getLatitud()
                                    + incrementoLat * i,
                            origen.getLongitud()
                                    + incrementoLon * i
                    );

            sugeridos.add(sugerido);
        }

        return sugeridos;
    }
}