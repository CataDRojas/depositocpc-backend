package cl.empresa.depositocpc.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Valida que el número de contenedor cumpla ISO 6346:
 * formato de 11 caracteres (4 letras + 7 dígitos) y dígito verificador correcto.
 */
@Documented
@Constraint(validatedBy = NumeroContenedorValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NumeroContenedorValido {

    String message() default "El número de contenedor no cumple el formato ISO 6346 (4 letras + 7 dígitos) o su dígito verificador es inválido";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
