package com.eden.web;

import com.eden.excepcion.RecursoNoEncontradoException;
import com.eden.excepcion.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Errores de {@link ApiController} en JSON: {"error": "...", "errores": {"campo": "..."}}.
 * Tiene prioridad sobre {@link ManejadorGlobalErrores}, que responde con la pagina HTML.
 */
@RestControllerAdvice(assignableTypes = ApiController.class)
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ApiErrores {

    private static final Logger log = LoggerFactory.getLogger(ApiErrores.class);

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<Map<String, Object>> reglaNegocio(ReglaNegocioException e) {
        Map<String, String> errores = new LinkedHashMap<>();
        if (e.tieneCampo()) {
            errores.put(e.getCampo(), e.getMessage());
        }
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, e.getMessage(), errores);
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> noEncontrado(RecursoNoEncontradoException e) {
        return respuesta(HttpStatus.NOT_FOUND, e.getMessage(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> inesperado(Exception e) {
        log.error("Error inesperado en la API", e);
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrió un error inesperado. Intenta de nuevo.", Map.of());
    }

    static ResponseEntity<Map<String, Object>> validacion(BindingResult resultado) {
        Map<String, String> errores = new LinkedHashMap<>();
        for (FieldError error : resultado.getFieldErrors()) {
            errores.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        String general = resultado.getAllErrors().get(0).getDefaultMessage();
        return respuesta(HttpStatus.UNPROCESSABLE_ENTITY, general, errores);
    }

    private static ResponseEntity<Map<String, Object>> respuesta(HttpStatus estado, String mensaje, Map<String, String> errores) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("error", mensaje);
        cuerpo.put("errores", errores);
        return ResponseEntity.status(estado).body(cuerpo);
    }
}
