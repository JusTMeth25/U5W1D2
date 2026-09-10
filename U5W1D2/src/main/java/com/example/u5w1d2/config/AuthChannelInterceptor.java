package com.example.u5w1d2.config;

import com.example.u5w1d2.auth.TokenStore;
import com.example.u5w1d2.logging.LoggingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ExecutorChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * Da' un'identita' alla connessione WebSocket: sul frame CONNECT legge il token
 * dall'header Authorization e associa lo username alla sessione. Senza questo,
 * le notifiche personali (/user/queue/...) non raggiungono nessuno.
 * <p>
 * Si occupa anche del contesto di logging del canale. Implementa
 * {@link ExecutorChannelInterceptor} e non il solo {@code ChannelInterceptor}
 * perche' i frame vengono consegnati agli handler su thread diversi da quello
 * che li riceve: senza beforeHandle quei thread scriverebbero righe senza id.
 */
@Component
public class AuthChannelInterceptor implements ExecutorChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(AuthChannelInterceptor.class);

    private final TokenStore tokens;

    public AuthChannelInterceptor(TokenStore tokens) {
        this.tokens = tokens;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // getAccessor, non StompHeaderAccessor.wrap: wrap lavora su una copia e setUser non avrebbe effetto.
        StompHeaderAccessor acc = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (acc == null) {
            return message;
        }

        // Il frame gira su un thread del canale, non su quello della richiesta HTTP:
        // gli diamo un id proprio, cosi' anche il traffico WebSocket e' tracciabile.
        String sessione = acc.getSessionId();
        LoggingContext.open(sessione == null ? null : "ws-" + sessione);
        Principal utenteDellaSessione = acc.getUser();
        if (utenteDellaSessione != null) {
            LoggingContext.setUser(utenteDellaSessione.getName());
        }

        if (StompCommand.CONNECT.equals(acc.getCommand())) {
            String header = acc.getFirstNativeHeader("Authorization");
            if (header == null || !header.startsWith("Bearer ")) {
                log.warn("CONNECT senza token: sessione senza utente, le notifiche personali verranno scartate");
                return message;
            }
            String token = header.substring(7);
            tokens.utenteDi(token).ifPresentOrElse(
                    username -> {
                        acc.setUser(() -> username);
                        LoggingContext.setUser(username);
                        log.info("CONNECT accettato per '{}'", username);
                    },
                    () -> log.warn("CONNECT con token non valido: sessione senza utente"));
        } else if (StompCommand.SUBSCRIBE.equals(acc.getCommand())) {
            log.debug("SUBSCRIBE a '{}'", acc.getDestination());
        } else if (StompCommand.DISCONNECT.equals(acc.getCommand())) {
            log.info("DISCONNECT della sessione WebSocket");
        }
        return message;
    }

    /**
     * Il thread del canale viene riusato dal frame successivo: senza questa pulizia
     * le righe di log erediterebbero utente e id della sessione precedente.
     */
    @Override
    public void afterSendCompletion(Message<?> message, MessageChannel channel, boolean sent, Exception ex) {
        if (ex != null) {
            log.error("Invio del frame fallito sul canale: {}", ex.getMessage(), ex);
        }
        LoggingContext.close();
    }

    /** Riapre il contesto sul thread dell'handler, che e' un altro thread ancora. */
    @Override
    public Message<?> beforeHandle(Message<?> message, MessageChannel channel, MessageHandler handler) {
        StompHeaderAccessor acc = StompHeaderAccessor.wrap(message);
        String sessione = acc.getSessionId();
        LoggingContext.open(sessione == null ? null : "ws-" + sessione);
        Principal utente = acc.getUser();
        if (utente != null) {
            LoggingContext.setUser(utente.getName());
        }
        return message;
    }

    @Override
    public void afterMessageHandled(Message<?> message, MessageChannel channel, MessageHandler handler, Exception ex) {
        if (ex != null) {
            log.error("Gestione del frame fallita: {}", ex.getMessage(), ex);
        }
        LoggingContext.close();
    }
}
