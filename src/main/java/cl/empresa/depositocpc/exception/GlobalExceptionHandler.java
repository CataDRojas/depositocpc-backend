package cl.empresa.depositocpc.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Manejo centralizado de errores. Todas las respuestas de error usan el mismo
 * formato: { codigo, mensaje, campo }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record RespuestaError(String codigo, String mensaje, String campo) {
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<RespuestaError> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return responder(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage(), null);
    }

    @ExceptionHandler(ConflictoException.class)
    public ResponseEntity<RespuestaError> manejarConflicto(ConflictoException ex) {
        return responder(HttpStatus.CONFLICT, "CONFLICTO", ex.getMessage(), null);
    }

    @ExceptionHandler(DespachoBloqueadoException.class)
    public ResponseEntity<RespuestaError> manejarDespachoBloqueado(DespachoBloqueadoException ex) {
        return responder(HttpStatus.CONFLICT, "DESPACHO_BLOQUEADO", ex.getMessage(), null);
    }

    @ExceptionHandler(PosicionLlenaException.class)
    public ResponseEntity<RespuestaError> manejarPosicionLlena(PosicionLlenaException ex) {
        return responder(HttpStatus.CONFLICT, "POSICION_LLENA", ex.getMessage(), null);
    }

    @ExceptionHandler(PeticionInvalidaException.class)
    public ResponseEntity<RespuestaError> manejarPeticionInvalida(PeticionInvalidaException ex) {
        return responder(HttpStatus.BAD_REQUEST, "PETICION_INVALIDA", ex.getMessage(), ex.getCampo());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespuestaError> manejarValidacionFallida(MethodArgumentNotValidException ex) {
        var erroresCampo = ex.getBindingResult().getFieldErrors();
        String campo = erroresCampo.isEmpty() ? null : erroresCampo.getFirst().getField();
        String mensajes = erroresCampo.stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .reduce((a, b) -> a + "; " + b)
                .orElse("La petición contiene datos inválidos");
        return responder(HttpStatus.BAD_REQUEST, "VALIDACION_FALLIDA", mensajes, campo);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<RespuestaError> manejarCuerpoIlegible(HttpMessageNotReadableException ex) {
        return responder(HttpStatus.BAD_REQUEST, "CUERPO_INVALIDO",
                "El cuerpo de la petición está malformado o contiene un valor inválido", "cuerpo");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<RespuestaError> manejarTipoParametroInvalido(MethodArgumentTypeMismatchException ex) {
        return responder(HttpStatus.BAD_REQUEST, "PARAMETRO_INVALIDO",
                "El parámetro '" + ex.getName() + "' tiene un valor inválido", ex.getName());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<RespuestaError> manejarViolacionIntegridad(DataIntegrityViolationException ex) {
        LOG.warn("Violación de integridad de datos: {}", ex.getMessage());
        return responder(HttpStatus.CONFLICT, "DATO_DUPLICADO",
                "Ya existe un registro con los datos proporcionados", null);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<RespuestaError> manejarCredencialesInvalidas(BadCredentialsException ex) {
        return responder(HttpStatus.UNAUTHORIZED, "CREDENCIALES_INVALIDAS",
                "Email o contraseña incorrectos", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespuestaError> manejarErrorInesperado(Exception ex) {
        LOG.error("Error inesperado no controlado", ex);
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "ERROR_INTERNO",
                "Ocurrió un error inesperado. Intente nuevamente más tarde.", null);
    }

    private ResponseEntity<RespuestaError> responder(HttpStatus estado, String codigo, String mensaje, String campo) {
        return ResponseEntity.status(estado).body(new RespuestaError(codigo, mensaje, campo));
    }
}
