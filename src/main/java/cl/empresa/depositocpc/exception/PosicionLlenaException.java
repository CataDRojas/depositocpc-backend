package cl.empresa.depositocpc.exception;

/**
 * Se lanza cuando se intenta colocar un contenedor en una posición
 * que ya alcanzó la altura máxima de apilamiento.
 */
public class PosicionLlenaException extends RuntimeException {

    public PosicionLlenaException(String mensaje) {
        super(mensaje);
    }
}
