package com.example.u5w1d2.logging;

import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Il contesto di logging vive sul thread: un task sottomesso a un executor girerebbe
 * su un altro thread e perderebbe l'id della richiesta. Questo decoratore copia il
 * contesto del chiamante sul thread del pool, gli assegna il proprio id di thread e
 * ripulisce alla fine, cosi' il thread riusato non eredita nulla.
 * <p>
 * Spring Boot lo applica da solo all'executor auto-configurato: c'e' gia' pronto
 * per quando l'applicazione iniziera' a usare {@code @Async}.
 */
@Component
public class MdcTaskDecorator implements TaskDecorator {

    @Override
    public Runnable decorate(Runnable runnable) {
        Map<String, String> contestoDelChiamante = MDC.getCopyOfContextMap();
        return () -> {
            try {
                LoggingContext.adopt(contestoDelChiamante);
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
