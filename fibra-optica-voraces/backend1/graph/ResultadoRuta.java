import java.util.List;

public class ResultadoRuta {

    private List<Arista> conexiones;
    private double costoTotal;
    private double distanciaTotal;
    private int cantidadPostes;

    public ResultadoRuta(List<Arista> conexiones,
                         double costoTotal,
                         double distanciaTotal,
                         int cantidadPostes) {

        this.conexiones = conexiones;
        this.costoTotal = costoTotal;
        this.distanciaTotal = distanciaTotal;
        this.cantidadPostes = cantidadPostes;
    }

    public List<Arista> getConexiones() {
        return conexiones;
    }

    public double getCostoTotal() {
        return costoTotal;
    }

    public double getDistanciaTotal() {
        return distanciaTotal;
    }

    public int getCantidadPostes() {
        return cantidadPostes;
    }
}