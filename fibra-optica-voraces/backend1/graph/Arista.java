public class Arista {

    private Nodo origen;
    private Nodo destino;
    private double distancia;
    private double costo;

    public Arista(Nodo origen, Nodo destino, double distancia) {

        this.origen = origen;
        this.destino = destino;
        this.distancia = distancia;
        this.costo = distancia;
    }

    public Nodo getOrigen() {
        return origen;
    }

    public Nodo getDestino() {
        return destino;
    }

    public double getDistancia() {
        return distancia;
    }

    public double getCosto() {
        return costo;
    }

    public double calcularCosto() {

        CostCalculator calculator =
                new CostCalculator();

        this.costo = calculator.calcularCosto(
                origen,
                destino,
                distancia
        );

        return costo;
    }
}