package com.ks.tocho5.config;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Fabrica los AuthenticationEntryPoint (401) y AccessDeniedHandler (403)
 * para que Spring Security responda JSON con el MISMO formato que
 * GlobalExceptionHandler (code, traceId, message).
 */
@Component
public class SecurityErrorHandlers {

  private final ObjectMapper mapper;

  public SecurityErrorHandlers(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public AuthenticationEntryPoint entryPoint() {
    return (req, res, ex) -> writeJson(res, HttpStatus.UNAUTHORIZED,
        GlobalExceptionHandler.CODE_UNAUTHENTICATED, resolveAuthMessage(ex));
  }

  public AccessDeniedHandler accessDeniedHandler() {
    return (req, res, ex) -> writeJson(res, HttpStatus.FORBIDDEN,
        GlobalExceptionHandler.CODE_FORBIDDEN, "Sin permisos para esta operación");
  }

  private String resolveAuthMessage(AuthenticationException ex) {
    // Mensaje corto, sin filtrar detalles del token
    if (ex == null) return "No autenticado";
    String m = ex.getMessage();
    if (m == null || m.isBlank()) return "No autenticado";
    // Evita mandar trazas internas
    if (m.toLowerCase().contains("token")) return "Token inválido o ausente";
    return "No autenticado";
  }

  private void writeJson(HttpServletResponse res, HttpStatus status, String code, String message) throws IOException {
    res.setStatus(status.value());
    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("timestamp", Instant.now().toString());
    body.put("status", status.value());
    body.put("error", status.getReasonPhrase());
    body.put("code", code);
    body.put("message", message);
    String traceId = MDC.get(RequestIdFilter.MDC_KEY);
    if (traceId != null) body.put("traceId", traceId);
    mapper.writeValue(res.getOutputStream(), body);
  }
}
