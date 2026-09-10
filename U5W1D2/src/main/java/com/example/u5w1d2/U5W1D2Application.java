package com.example.u5w1d2;

import com.example.u5w1d2.logging.LoggingContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class U5W1D2Application {

    public static void main(String[] args) {
        // Il contesto si apre prima di Spring: da questo momento ogni riga emessa dal
        // thread di avvio porta id del thread e rid=startup, comprese quelle di Spring.
        LoggingContext.open("startup");
        try {
            SpringApplication.run(U5W1D2Application.class, args);
        } finally {
            LoggingContext.close();
        }
    }

}
