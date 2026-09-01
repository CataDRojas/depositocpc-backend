package cl.empresa.depositocpc.exception;

public class PeticionInvalidaException extends RuntimeException {

    private final String campo;

    public PeticionInvalidaException(String mensaje) {
        this(mensaje, null);
    }

    public PeticionInvalidaException(String mensaje, String campo) {
        super(mensaje);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
