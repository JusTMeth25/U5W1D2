-- Tre topic di partenza. ON CONFLICT: idempotente ad ogni riavvio (ddl-auto=update conserva i dati).
INSERT INTO topics (name, title, description) VALUES
    ('java', 'Java', 'Discussioni su Java, JVM e librerie.')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO topics (name, title, description) VALUES
    ('spring', 'Spring', 'Spring Boot, Data, Security e dintorni.')
    ON CONFLICT (name) DO NOTHING;
INSERT INTO topics (name, title, description) VALUES
    ('frontend', 'Frontend', 'React, Vite e sviluppo lato client.')
    ON CONFLICT (name) DO NOTHING;
