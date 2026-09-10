package com.example.u5w1d2.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Primo filtro della catena: apre il contesto di logging sul thread che serve la
 * richiesta e lo chiude a richiesta conclusa. Sta prima di Spring Security cosi'
 * anche i rifiuti della catena di sicurezza finiscono nel log con il loro id.
 * <p>
 * Livelli: 2xx/3xx INFO, 4xx WARN (colpa del chiamante, il servizio sta bene),
 * 5xx ERROR (colpa nostra, va guardato).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Se il chiamante porta gia' un id lo riusiamo: la stessa richiesta resta
        // tracciabile dal frontend al backend con una sola chiave.
        String requestId = LoggingContext.open(request.getHeader(LoggingContext.REQUEST_ID_HEADER));
        response.setHeader(LoggingContext.REQUEST_ID_HEADER, requestId);

        String metodo = request.getMethod();
        String percorso = percorsoCompleto(request);
        long inizio = System.nanoTime();

        try {
            chain.doFilter(request, response);
        } finally {
            long durataMs = (System.nanoTime() - inizio) / 1_000_000;
            int status = response.getStatus();
            if (status >= 500) {
                log.error("{} {} -> {} in {} ms", metodo, percorso, status, durataMs);
            } else if (status >= 400) {
                log.warn("{} {} -> {} in {} ms", metodo, percorso, status, durataMs);
            } else {
                log.info("{} {} -> {} in {} ms", metodo, percorso, status, durataMs);
            }
            LoggingContext.close();
        }
    }

    /**
     * Fuori dal log: il trasporto SockJS (una POST di polling ogni pochi secondi per
     * ogni sessione aperta, sempre 200) e le risorse statiche. Cio' che conta del
     * canale WebSocket lo registra AuthChannelInterceptor sui frame STOMP.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/ws/") || uri.startsWith("/assets/") || uri.equals("/favicon.ico");
    }

    private String percorsoCompleto(HttpServletRequest request) {
        String query = request.getQueryString();
        return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
    }
}
