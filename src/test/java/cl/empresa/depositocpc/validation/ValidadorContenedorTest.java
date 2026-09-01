package cl.empresa.depositocpc.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorContenedorTest {

    @ParameterizedTest(name = "{0} es válido")
    @ValueSource(strings = {"CSQU3054383", "MSKU1234565", "TEST1234560"})
    @DisplayName("Números de contenedor ISO 6346 válidos")
    void numerosValidos(String numero) {
        assertTrue(ValidadorContenedor.esValido(numero));
    }

    @ParameterizedTest(name = "{0} es inválido")
    @ValueSource(strings = {
            "CSQU3054382",      // dígito verificador incorrecto
            "CSQU305438",       // faltan dígitos
            "CSQU30543833",     // sobran caracteres
            "csqu3054383",      // minúsculas
            "CSQ13054383",      // 5 letras
            "C5QU3054383",      // dígito en posición de letra
            "12345678901"       // solo números
    })
    @DisplayName("Números de contenedor inválidos")
    void numerosInvalidos(String numero) {
        assertFalse(ValidadorContenedor.esValido(numero));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("Nulos y vacíos son inválidos")
    void nulosOVacios(String numero) {
        assertFalse(ValidadorContenedor.esValido(numero));
    }

    @Test
    @DisplayName("El mapa de valores de letras salta los múltiplos de 11")
    void valoresDeLetrasSaltanMultiplosDeOnce() {
        // A=10, K=21 y U=32 son los puntos donde se salta un múltiplo de 11 (11, 22 y 33)
        char[] letrasEsperadas = {'A', 'K', 'U'};
        int[] valoresEsperados = {10, 21, 32};
        for (int i = 0; i < letrasEsperadas.length; i++) {
            assertTrue(ValidadorContenedor.esValido(numeroConLetras(letrasEsperadas[i], valoresEsperados[i])),
                    "La letra " + letrasEsperadas[i] + " debería valer " + valoresEsperados[i]);
        }
    }

    /**
     * Construye un número válido usando el valor conocido de una letra:
     * usa la letra en las 4 primeras posiciones y calcula el dígito verificador.
     */
    private String numeroConLetras(char letra, int valorLetra) {
        StringBuilder base = new StringBuilder();
        int suma = 0;
        for (int posicion = 0; posicion < 10; posicion++) {
            int valor = posicion < 4 ? valorLetra : Character.getNumericValue("000000".charAt(posicion - 4));
            suma += valor * (int) Math.pow(2, posicion);
            base.append(posicion < 4 ? letra : '0');
        }
        int resto = suma % 11;
        int digito = resto == 10 ? 0 : resto;
        return base.append(digito).toString();
    }
}
