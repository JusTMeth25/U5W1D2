package com.example.u5w1d2.logging;

import org.slf4j.MDC;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;

/**
 * Contesto diagnostico condiviso da tutti i log dell'applicazione.
 * <p>
 * Ogni unita' di lavoro (avvio, richiesta HTTP, frame STOMP, task asincrono) apre
 * il contesto sul proprio thread: da quel momento ogni riga di log emessa da quel
 * thread porta id del thread, id della richiesta e utente, senza che il codice di
 * dominio debba ripeterli in ogni messaggio.
 * <p>
 * Il contesto va sempre chiuso in un blocco finally: i thread dei pool vengono
 * riusati e un MDC lasciato sporco attribuirebbe le righe successive alla
 * richiesta sbagliata.
 * <p>
 * I contesti si annidano. Con il trasporto SockJS, per esempio, un frame STOMP
 * viene consegnato sullo stesso thread Tomcat che sta servendo la POST che lo
 * trasporta: il contesto del frame si apre sopra quello della richiesta e alla
 * chiusura quello della richiesta torna al suo posto, invece di sparire.
 */
public final class LoggingContext {

    public static final String REQUEST_ID = "requestId";
    public static final String THREAD_ID = "threadId";
    public static final String USER = "user";

    /** Header con cui il client puo' imporre il proprio id di correlazione, e con cui glielo restituiamo. */
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    /** Per ogni thread, i contesti da ripristinare alla chiusura di quelli aperti sopra. */
    private static final ThreadLocal<Deque<Map<String, String>>> CONTESTI_SOSPESI =
            ThreadLocal.withInitial(ArrayDeque::new);

    private LoggingContext() {
    }

    /**
     * Apre il contesto sul thread corrente assegnandogli l'id indicato.
     * Se l'id e' nullo o vuoto ne viene generato uno nuovo.
     *
     * @return l'id effettivamente in uso
     */
    public static String open(String requestId) {
        Map<String, String> contestoPrecedente = MDC.getCopyOfContextMap();
        CONTESTI_SOSPESI.get().push(contestoPrecedente == null ? Map.of() : contestoPrecedente);

        String id = (requestId == null || requestId.isBlank()) ? newId() : requestId;
        MDC.put(REQUEST_ID, id);
        MDC.put(THREAD_ID, String.valueOf(Thread.currentThread().threadId()));
        MDC.remove(USER);
        return id;
    }

    /** Apre il contesto con un id generato. */
    public static String open() {
        return open(null);
    }

    /** Registra l'utente autenticato: da qui in avanti ogni riga del thread lo riporta. */
    public static void setUser(String username) {
        if (username != null && !username.isBlank()) {
            MDC.put(USER, username);
        }
    }

    /** Id della richiesta in corso, o null fuori da un contesto aperto. */
    public static String currentRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * Chiude il contesto e rimette quello che c'era prima. Obbligatorio in finally:
     * i thread dei pool vengono riusati.
     */
    public static void close() {
        Deque<Map<String, String>> pila = CONTESTI_SOSPESI.get();
        Map<String, String> contestoPrecedente = pila.poll();
        if (contestoPrecedente == null || contestoPrecedente.isEmpty()) {
            MDC.clear();
        } else {
            MDC.setContextMap(contestoPrecedente);
        }
        if (pila.isEmpty()) {
            CONTESTI_SOSPESI.remove();
        }
    }

    /**
     * Copia il contesto del thread chiamante e lo installa sul thread corrente,
     * aggiornando l'id del thread. Serve ai task asincroni, che girano su un
     * thread diverso da quello che li ha sottomessi.
     */
    public static void adopt(Map<String, String> contestoDelChiamante) {
        if (contestoDelChiamante == null) {
            MDC.clear();
        } else {
            MDC.setContextMap(contestoDelChiamante);
        }
        MDC.put(THREAD_ID, String.valueOf(Thread.currentThread().threadId()));
    }

    private static String newId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
