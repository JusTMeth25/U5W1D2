package com.example.u5w1d2.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Riassume in tre righe lo stato dell'istanza al momento in cui inizia ad
 * accettare traffico: in produzione e' la prima cosa che si legge quando si apre
 * il log di un pod o di un servizio appena riavviato.
 */
@Component
public class StartupLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupLogger.class);

    private final Environment env;

    public StartupLogger(Environment env) {
        this.env = env;
    }

    @EventListener
    public void onReady(ApplicationReadyEvent event) {
        // L'evento arriva sul thread di avvio: apriamo il contesto qui cosi' anche
        // le righe di avvio hanno id di thread e id di correlazione.
        LoggingContext.open("startup");
        try {
            String[] profiliAttivi = env.getActiveProfiles();
            Duration avvio = event.getTimeTaken();

            log.info("Applicazione '{}' avviata in {} ms su porta {} (contesto {})",
                    env.getProperty("spring.application.name", "U5W1D2"),
                    avvio == null ? "?" : avvio.toMillis(),
                    env.getProperty("server.port", "8080"),
                    env.getProperty("server.servlet.context-path", "/"));

            if (profiliAttivi.length == 0) {
                log.warn("Nessun profilo Spring attivo: valgono i default di application.properties. "
                        + "In produzione avviare con --spring.profiles.active=prod");
            } else {
                log.info("Profili attivi: {}", String.join(", ", profiliAttivi));
            }

            log.info("Log su console e su file '{}'; ogni riga porta tid (id thread), rid (id richiesta) e user",
                    env.getProperty("logging.file.name", "logs/u5w1d2.log"));
        } finally {
            LoggingContext.close();
        }
    }

    @EventListener
    public void onFailure(ApplicationFailedEvent event) {
        LoggingContext.open("startup");
        try {
            log.error("Avvio fallito: {}", event.getException().getMessage(), event.getException());
        } finally {
            LoggingContext.close();
        }
    }

    @EventListener
    public void onShutdown(ContextClosedEvent event) {
        LoggingContext.open("shutdown");
        try {
            log.info("Arresto in corso: i token in memoria decadono, le sessioni WebSocket vengono chiuse");
        } finally {
            LoggingContext.close();
        }
    }
}
