// src/main/java/com/ks/tocho5/config/GlobalExceptionHandler.java
package com.ks.tocho5.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Centraliza el manejo de excepciones:
 *  - No expone stack traces al cliente.
 *  - Loguea el detalle real en el servidor.
 *  - Devuelve JSON consistente: { timestamp, status, error, message }.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // -------- 400 / Validación --------
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return build(HttpStatus.BAD_REQUEST, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .orElse("Datos inválidos");
    return build(HttpStatus.BAD_REQUEST, msg);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
    return build(HttpStatus.PAYLOAD_TOO_LARGE, "Archivo demasiado grande");
  }

  // -------- 401 / 403 --------
  @ExceptionHandler({ AuthenticationException.class,
                      AuthenticationCredentialsNotFoundException.class,
                      InvalidBearerTokenException.class })
  public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) {
    return build(HttpStatus.UNAUTHORIZED, "No autenticado");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
    return build(HttpStatus.FORBIDDEN, "Sin permisos para esta operación");
  }

  // -------- 409 (Integridad de datos) --------
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
    log.warn("Integrity violation", ex);
    return build(HttpStatus.CONFLICT, "Conflicto de datos");
  }

  // -------- 503 (Servicio no configurado / dependencia caída) --------
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
    log.warn("Servicio no disponible: {}", ex.getMessage());
    return build(HttpStatus.SERVICE_UNAVAILABLE,
        ex.getMessage() == null ? "Servicio no disponible" : ex.getMessage());
  }

  // -------- ResponseStatusException --------
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
    return build(status, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason());
  }

  // -------- Runtime (regla de negocio con throw new RuntimeException(...)) --------
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
    // Los throw new RuntimeException("...") del código los tratamos como 400 (regla de negocio)
    // pero logueamos stack trace a nivel WARN para no perderlo.
    log.warn("Business rule violation: {}", ex.getMessage(), ex);
    String msg = ex.getMessage();
    if (msg == null || msg.isBlank()) msg = "Operación no válida";
    return build(HttpStatus.BAD_REQUEST, msg);
  }

  // -------- 500 fallback --------
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
    log.error("Unhandled error", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, "Error interno");
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("message", message == null ? status.getReasonPhrase() : message);
    return ResponseEntity.status(status).body(body);
  }
}
