package com.ks.tocho5.config;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Asigna un X-Request-Id a cada request y lo guarda en MDC para que
 * aparezca en todos los logs hechos durante el procesamiento.
 *
 * - Si el cliente envía X-Request-Id, lo respetamos.
 * - El valor también se devuelve como response header para poder correlacionar
 *   front <-> back.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String HEADER = "X-Request-Id";
  public static final String MDC_KEY = "requestId";
  public static final String ATTRIBUTE = "tocho5.requestId";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain chain) throws ServletException, IOException {
    String id = request.getHeader(HEADER);
    if (id == null || id.isBlank() || id.length() > 64) {
      id = UUID.randomUUID().toString();
    }
    MDC.put(MDC_KEY, id);
    request.setAttribute(ATTRIBUTE, id);
    response.setHeader(HEADER, id);
    try {
      chain.doFilter(request, response);
    } finally {
      MDC.remove(MDC_KEY);
    }
  }
}
