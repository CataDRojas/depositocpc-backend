package cl.empresa.depositocpc.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Adaptador Bean Validation que delega en {@link ValidadorContenedor}.
 */
public class NumeroContenedorValidator implements ConstraintValidator<NumeroContenedorValido, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext contexto) {
        if (valor == null || valor.isBlank()) {
            return true;
        }
        return ValidadorContenedor.esValido(valor);
    }
}
