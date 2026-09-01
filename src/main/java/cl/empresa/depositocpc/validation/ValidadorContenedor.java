package cl.empresa.depositocpc.validation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Validador del número de contenedor según ISO 6346.
 *
 * <p>Reglas:
 * <ol>
 *   <li>Formato: 4 letras mayúsculas + 7 dígitos (regex ^[A-Z]{4}[0-9]{7}$).</li>
 *   <li>Cada letra tiene un valor asignado (A=10, B=12, ... Z=38), saltando los múltiplos de 11.</li>
 *   <li>Los primeros 10 caracteres se multiplican por 2 elevado a su posición (0 a 9).</li>
 *   <li>La suma se divide entre 11; si el resto es 10, el dígito esperado es 0.</li>
 *   <li>El dígito esperado debe coincidir con el último carácter del número.</li>
 * </ol>
 */
public final class ValidadorContenedor {

    private static final Pattern PATRON_ISO_6346 = Pattern.compile("^[A-Z]{4}\\d{7}$");
    private static final Map<Character, Integer> VALORES_LETRAS = new HashMap<>(26);

    static {
        int valor = 10;
        for (char letra = 'A'; letra <= 'Z'; letra++) {
            if (valor % 11 == 0) {
                valor++;
            }
            VALORES_LETRAS.put(letra, valor);
            valor++;
        }
    }

    private ValidadorContenedor() {
    }

    public static boolean esValido(String numeroContenedor) {
        if (numeroContenedor == null || !PATRON_ISO_6346.matcher(numeroContenedor).matches()) {
            return false;
        }
        int suma = 0;
        for (int posicion = 0; posicion < 10; posicion++) {
            char caracter = numeroContenedor.charAt(posicion);
            int valorCaracter = posicion < 4
                    ? VALORES_LETRAS.get(caracter)
                    : Character.getNumericValue(caracter);
            suma += valorCaracter * (int) Math.pow(2, posicion);
        }
        int resto = suma % 11;
        int digitoEsperado = resto == 10 ? 0 : resto;
        int digitoReal = Character.getNumericValue(numeroContenedor.charAt(10));
        return digitoEsperado == digitoReal;
    }
}
