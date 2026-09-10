# U5W1D2 — Bacheche con notifiche in tempo reale

Backend Spring Boot con STOMP su WebSocket e frontend React (Vite) nella stessa cartella.

Chi è iscritto a una bacheca può scrivere; ogni messaggio pubblicato viaggia su due
destinazioni: il feed pubblico della bacheca (`/topic/feed/{nome}`) e la coda personale di
ogni altro iscritto (`/user/queue/notifications`). Le notifiche vengono anche salvate su
database, così il contatore della campanella resta corretto dopo un refresh o una sessione chiusa.

## Struttura

```
U5W1D2/          backend Spring Boot (porta 3001)
frontend/        client React + Vite (porta 5173)
```

## Requisiti

- JDK 25
- Node.js 20+
- PostgreSQL in ascolto su `localhost:5432` con un database chiamato `U5W1D2`

## Avvio del backend

Crea il file `U5W1D2/env.properties` (è escluso da git) con le due variabili usate da
`application.properties`:

```properties
DB_PASSWORD=la-tua-password
JWT_SECRET=una-stringa-qualsiasi
```

Il file va nella cartella da cui parte il processo, perché `spring.config.import` lo cerca
con `file:env.properties`. Poi:

```bash
cd U5W1D2
./mvnw spring-boot:run
```

Il backend ascolta su `http://localhost:3001`. Allo start `data.sql` inserisce le tre
bacheche iniziali (`java`, `spring`, `frontend`) in modo idempotente.

## Avvio del frontend

```bash
cd frontend
npm install
npm run dev
```

Il client parte su `http://localhost:5173`, che è l'unica origine accettata dal backend
(`app.cors.allowed-origin` in `application.properties`). La porta è fissata in
`vite.config.js` con `strictPort`, così un cambio silenzioso di porta non fa fallire il
CORS. L'indirizzo del backend si cambia in `frontend/.env` (`VITE_API_URL`).

## Endpoint REST

| Metodo | Percorso | Descrizione |
| --- | --- | --- |
| POST | `/api/auth/register` | Registrazione, risponde con `{ username, token }` |
| POST | `/api/auth/login` | Login |
| GET | `/api/topics` | Bacheche con numero di iscritti e stato dell'iscrizione |
| POST | `/api/topics/{nome}/subscription` | Iscrizione |
| DELETE | `/api/topics/{nome}/subscription` | Disiscrizione |
| GET | `/api/topics/{nome}/messages` | Storico paginato |
| POST | `/api/topics/{nome}/messages` | Pubblicazione (solo iscritti) |
| GET | `/api/notifications` | Notifiche dell'utente, paginate |
| GET | `/api/notifications/unread-count` | Contatore delle non lette |
| POST | `/api/notifications/read-all` | Segna tutte come lette |

Il token è opaco e vive in memoria nel backend: al riavvio del processo decade e le
chiamate tornano `401`.

## WebSocket

- Endpoint: `http://localhost:3001/ws` con SockJS, oppure `ws://localhost:3001/ws/websocket`
  per un client STOMP puro.
- Il token va nell'header `Authorization: Bearer <token>` del frame `CONNECT`:
  `AuthChannelInterceptor` lo legge e lega lo username alla sessione. Senza quel passaggio
  le destinazioni `/user/...` non raggiungono nessuno.
- Destinazioni: `/topic/feed/{nome}` per il feed della bacheca,
  `/user/queue/notifications` per le notifiche personali.

## Verifica con curl

I codici qui sotto sono quelli osservati eseguendo la sequenza su un database pulito.

```bash
# 1. due utenti
curl -i -X POST localhost:3001/api/auth/register \
  -H 'Content-Type: application/json' -d '{"username":"mario","password":"password1"}'   # 201 + token
curl -i -X POST localhost:3001/api/auth/register \
  -H 'Content-Type: application/json' -d '{"username":"mario","password":"password1"}'   # 409 nome preso
curl -i -X POST localhost:3001/api/auth/register \
  -H 'Content-Type: application/json' -d '{"username":"lucia","password":"password1"}'   # 201 + token

MARIO=<token di mario>
LUCIA=<token di lucia>

# 2. iscrizioni
curl -i -X POST localhost:3001/api/topics/java/subscription -H "Authorization: Bearer $MARIO"       # 201
curl -i -X POST localhost:3001/api/topics/java/subscription -H "Authorization: Bearer $MARIO"       # 409
curl -i -X POST localhost:3001/api/topics/nonesiste/subscription -H "Authorization: Bearer $MARIO"  # 404
curl -i -X POST localhost:3001/api/topics/java/subscription -H "Authorization: Bearer $LUCIA"       # 201

# 3. messaggi
curl -i -X POST localhost:3001/api/topics/java/messages -H "Authorization: Bearer $MARIO" \
  -H 'Content-Type: application/json' -d '{"text":"ciao"}'    # 201, iscritto
curl -i -X POST localhost:3001/api/topics/java/messages -H "Authorization: Bearer $TERZO" \
  -H 'Content-Type: application/json' -d '{"text":"ciao"}'    # 403, non iscritto
curl -i -X POST localhost:3001/api/topics/java/messages \
  -H 'Content-Type: application/json' -d '{"text":"ciao"}'    # 401, senza token
curl -i -X POST localhost:3001/api/topics/java/messages -H "Authorization: Bearer $MARIO" \
  -H 'Content-Type: application/json' -d '{"text":""}'        # 400, testo vuoto

# 4. notifiche di lucia
curl -s localhost:3001/api/notifications/unread-count -H "Authorization: Bearer $LUCIA"   # {"count":1}
curl -s localhost:3001/api/notifications -H "Authorization: Bearer $LUCIA"
curl -i -X POST localhost:3001/api/notifications/read-all -H "Authorization: Bearer $LUCIA"  # 204
```

## Verifica con due browser

1. Registra `mario` e `lucia` in due finestre diverse e iscrivi entrambi a `java`.
2. Con tutte e due sulla pagina della bacheca, il messaggio di mario compare da lucia senza
   ricaricare.
3. Se lucia è sull'elenco delle bacheche, il messaggio di mario le accende il contatore
   della campanella.
4. Chiudendo la finestra di lucia, facendo scrivere mario e rientrando, il contatore c'è
   comunque: viene letto dal database.
5. Un terzo utente non iscritto non riceve niente e in scrittura ottiene `403`.
