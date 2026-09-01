package cl.empresa.depositocpc.exception;

/**
 * Se lanza cuando se intenta despachar un contenedor que no está
 * en la cima de su pila (violación de LIFO).
 */
public class DespachoBloqueadoException extends RuntimeException {

    private final int contenedoresEncima;

    public DespachoBloqueadoException(String mensaje, int contenedoresEncima) {
        super(mensaje);
        this.contenedoresEncima = contenedoresEncima;
    }

    public int getContenedoresEncima() {
        return contenedoresEncima;
    }
}
