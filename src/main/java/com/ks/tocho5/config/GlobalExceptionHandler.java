// src/main/java/com/ks/tocho5/config/GlobalExceptionHandler.java
package com.ks.tocho5.config;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Respuesta uniforme para TODOS los errores:
 *   {
 *     "timestamp": "...",
 *     "status": 400,
 *     "error": "Bad Request",
 *     "code": "BUSINESS_RULE",
 *     "message": "No puedes cambiar el logo de un equipo que no es tuyo",
 *     "traceId": "a1b2c3..."
 *   }
 *
 * El traceId también va en el header X-Request-Id (ver RequestIdFilter).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  // ---- Códigos de error estables (doc para el front) ----
  public static final String CODE_VALIDATION      = "VALIDATION";
  public static final String CODE_BUSINESS_RULE   = "BUSINESS_RULE";
  public static final String CODE_PAYLOAD_TOO_BIG = "PAYLOAD_TOO_BIG";
  public static final String CODE_NOT_FOUND       = "NOT_FOUND";
  public static final String CODE_METHOD_NOT_ALLOWED = "METHOD_NOT_ALLOWED";
  public static final String CODE_UNAUTHENTICATED = "UNAUTHENTICATED";
  public static final String CODE_FORBIDDEN       = "FORBIDDEN";
  public static final String CODE_CONFLICT        = "CONFLICT";
  public static final String CODE_UNAVAILABLE     = "SERVICE_UNAVAILABLE";
  public static final String CODE_INTERNAL        = "INTERNAL";

  // -------- 400 / Validación --------
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
    return build(HttpStatus.BAD_REQUEST, CODE_VALIDATION, ex.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .findFirst()
        .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
        .orElse("Datos inválidos");
    return build(HttpStatus.BAD_REQUEST, CODE_VALIDATION, msg);
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ResponseEntity<Map<String, Object>> handleTooLarge(MaxUploadSizeExceededException ex) {
    return build(HttpStatus.PAYLOAD_TOO_LARGE, CODE_PAYLOAD_TOO_BIG, "Archivo demasiado grande");
  }

  // -------- 401 / 403 --------
  @ExceptionHandler({ AuthenticationException.class,
                      AuthenticationCredentialsNotFoundException.class,
                      InvalidBearerTokenException.class })
  public ResponseEntity<Map<String, Object>> handleUnauthorized(Exception ex) {
    return build(HttpStatus.UNAUTHORIZED, CODE_UNAUTHENTICATED, "No autenticado");
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, Object>> handleForbidden(AccessDeniedException ex) {
    return build(HttpStatus.FORBIDDEN, CODE_FORBIDDEN, "Sin permisos para esta operación");
  }

  // -------- 404 / 405 --------
  @ExceptionHandler(NoHandlerFoundException.class)
  public ResponseEntity<Map<String, Object>> handleNotFound(NoHandlerFoundException ex) {
    return build(HttpStatus.NOT_FOUND, CODE_NOT_FOUND,
        "Ruta no encontrada: " + ex.getHttpMethod() + " " + ex.getRequestURL());
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {
    return build(HttpStatus.METHOD_NOT_ALLOWED, CODE_METHOD_NOT_ALLOWED,
        "Método no permitido: " + ex.getMethod());
  }

  // -------- 409 --------
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<Map<String, Object>> handleIntegrity(DataIntegrityViolationException ex) {
    log.warn("Integrity violation", ex);
    return build(HttpStatus.CONFLICT, CODE_CONFLICT, "Conflicto de datos");
  }

  // -------- 503 (servicio no configurado / dependencia caída) --------
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
    log.warn("Servicio no disponible: {}", ex.getMessage());
    return build(HttpStatus.SERVICE_UNAVAILABLE, CODE_UNAVAILABLE,
        ex.getMessage() == null ? "Servicio no disponible" : ex.getMessage());
  }

  // -------- ResponseStatusException --------
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, Object>> handleStatus(ResponseStatusException ex) {
    HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
    if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
    String code = switch (status.series()) {
      case CLIENT_ERROR -> CODE_VALIDATION;
      case SERVER_ERROR -> CODE_INTERNAL;
      default -> CODE_INTERNAL;
    };
    return build(status, code, ex.getReason() == null ? status.getReasonPhrase() : ex.getReason());
  }

  // -------- RuntimeException (regla de negocio con throw new RuntimeException(...)) --------
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex) {
    log.warn("Business rule violation: {}", ex.getMessage(), ex);
    String msg = ex.getMessage();
    if (msg == null || msg.isBlank()) msg = "Operación no válida";
    return build(HttpStatus.BAD_REQUEST, CODE_BUSINESS_RULE, msg);
  }

  // -------- 500 fallback --------
  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
    log.error("Unhandled error", ex);
    return build(HttpStatus.INTERNAL_SERVER_ERROR, CODE_INTERNAL, "Error interno");
  }

  private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("code", code);
    body.put("message", message == null ? status.getReasonPhrase() : message);
    String traceId = MDC.get(RequestIdFilter.MDC_KEY);
    if (traceId != null) body.put("traceId", traceId);
    return ResponseEntity.status(status).body(body);
  }
}
