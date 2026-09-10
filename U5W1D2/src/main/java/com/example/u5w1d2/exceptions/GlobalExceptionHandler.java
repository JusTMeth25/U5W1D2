package com.example.u5w1d2.exceptions;

import com.example.u5w1d2.logging.LoggingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unico punto in cui un'eccezione diventa una risposta HTTP, quindi unico punto
 * in cui vale la pena decidere il livello di log.
 * <p>
 * Criterio: 4xx sono errori del chiamante e si registrano a WARN senza stack trace
 * (il servizio sta funzionando come deve); 5xx sono errori nostri e si registrano
 * a ERROR con lo stack trace completo, perche' sono l'unica cosa che va indagata.
 * <p>
 * Nel corpo della risposta viaggia anche requestId: e' la chiave con cui ritrovare
 * nel log la riga esatta di quella richiesta.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> body(HttpStatusCode status, String message) {
        HttpStatus risolto = HttpStatus.resolve(status.value());
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("status", status.value());
        corpo.put("error", risolto != null ? risolto.getReasonPhrase() : "Error");
        corpo.put("message", message != null ? message : "Errore");
        String requestId = LoggingContext.currentRequestId();
        if (requestId != null) {
            corpo.put("requestId", requestId);
        }
        return ResponseEntity.status(status).body(corpo);
    }

    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<Map<String, Object>> handleUsernameTaken(UsernameTakenException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(InvalidCredentialsException ex) {
        log.warn("401 Unauthorized: credenziali non valide");
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        log.warn("401 Unauthorized: {}", ex.getMessage());
        return body(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(TopicNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleTopicNotFound(TopicNotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotificationNotFound(NotificationNotFoundException ex) {
        log.warn("404 Not Found: {}", ex.getMessage());
        return body(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(AlreadySubscribedException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadySubscribed(AlreadySubscribedException ex) {
        log.warn("409 Conflict: {}", ex.getMessage());
        return body(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(NotSubscribedException.class)
    public ResponseEntity<Map<String, Object>> handleNotSubscribed(NotSubscribedException ex) {
        log.warn("403 Forbidden: {}", ex.getMessage());
        return body(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("Richiesta non valida");
        log.warn("400 Bad Request: {} campo/i non validi, primo: {}",
                ex.getBindingResult().getErrorCount(), msg);
        return body(HttpStatus.BAD_REQUEST, msg);
    }

    /**
     * Rete di sicurezza. Le eccezioni che Spring MVC sa gia' tradurre in uno stato
     * (404 su rotta inesistente, 405, 415, ...) implementano ErrorResponse: restano
     * 4xx e restano a WARN. Tutto il resto e' un difetto del server: 500 e ERROR
     * con stack trace, e al client va un messaggio generico piu' il requestId.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        if (ex instanceof ErrorResponse errore) {
            HttpStatusCode status = errore.getStatusCode();
            if (status.is4xxClientError()) {
                log.warn("{}: {}", status.value(), ex.getMessage());
                return body(status, ex.getMessage());
            }
        }
        log.error("500 Internal Server Error: {}", ex.getMessage(), ex);
        return body(HttpStatus.INTERNAL_SERVER_ERROR,
                "Errore interno. Riferimento per l'assistenza: " + LoggingContext.currentRequestId());
    }
}
