public class DistanceValidator {

    public boolean esValida(
            Nodo origen,
            Nodo destino
    ) {

        double distancia =
                origen.distanciaA(destino);

        if (
                origen.getTipo() == TipoNodo.CENTRAL
                        &&
                        (
                                destino.getTipo() == TipoNodo.POSTE_PRINCIPAL
                                        ||
                                        destino.getTipo() == TipoNodo.POSTE_SECUNDARIO
                        )
        ) {
            return distancia <= 150;
        }

        if (
                (
                        origen.getTipo() == TipoNodo.POSTE_PRINCIPAL
                                ||
                                origen.getTipo() == TipoNodo.POSTE_SECUNDARIO
                )
                        &&
                        (
                                destino.getTipo() == TipoNodo.POSTE_PRINCIPAL
                                        ||
                                        destino.getTipo() == TipoNodo.POSTE_SECUNDARIO
                        )
        ) {
            return distancia <= 80;
        }

        if (
                (
                        origen.getTipo() == TipoNodo.POSTE_PRINCIPAL
                                ||
                                origen.getTipo() == TipoNodo.POSTE_SECUNDARIO
                )
                        &&
                        destino.getTipo() == TipoNodo.CLIENTE
        ) {
            return distancia <= 40;
        }

        return false;
    }
}