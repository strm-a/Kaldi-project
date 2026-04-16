# API Docs

Static endpoint reference for `kaldi-support-api`. Use this folder when you want
to review the API without starting the project.

## Base URL

Local API base URL: `http://localhost:8080`

## Authentication

| Path prefix   | Auth         | Notes                                                       |
|---------------|--------------|-------------------------------------------------------------|
| `/user/*`     | None         | The mobile app identifies the user by `userId` in the body. |
| `/operator/*` | Bearer JWT   | Token must come from Keycloak and include the `operator` role. |

Operator authentication is handled by Keycloak directly. There is no backend
`/auth` endpoint in this service.

## Dev Keycloak Operators

| Username        | Password | Role       |
|-----------------|----------|------------|
| `aliceOperator` | `alice`  | `operator` |
| `mikeOperator`  | `mike`   | `operator` |
| `lucyOperator`  | `lucy`   | `operator` |

## Enums

- `Room`: `TEHNIKA`, `STORITVE`, `POGOVOR`
- `ChatStatus`: `WAITING`, `ACTIVE`, `CLOSED`
- `SenderType`: `USER`, `OPERATOR`

## Endpoints

### `POST /user/chat`

Creates a new `WAITING` chat and stores the first user message.

Auth: none

Request body:

```json
{
  "userId": 1,
  "room": "TEHNIKA",
  "message": "Hi, my router keeps disconnecting."
}
```

Success: `200 OK` with a `Chat` response

Errors: `404 User not found`

### `POST /user/chat/{chatId}/message`

Adds a new user message to an existing chat.

Auth: none

Path parameter: `chatId`

Request body:

```json
{
  "message": "The connection dropped again."
}
```

Success: `200 OK` with a `Message` response

Errors: `404 Chat not found`

### `GET /user/chat/{chatId}/messages`

Returns every message in the chat ordered from oldest to newest.

Auth: none

Path parameter: `chatId`

Success: `200 OK` with `Message[]`

Errors: `404 Chat not found`

### `GET /operator/chats`

Returns all open chats across the dashboard. The list includes chats in
`WAITING` and `ACTIVE` status.

Auth: Bearer JWT with role `operator`

Success: `200 OK` with `ChatSummaryDTO[]`

Errors: `401 Missing or invalid JWT`, `403 Missing operator role`

### `POST /operator/chats/{chatId}/acquire`

Assigns a waiting chat to the authenticated operator and moves it to `ACTIVE`.

Auth: Bearer JWT with role `operator`

Path parameter: `chatId`

Success: `200 OK` with `ChatDetailDTO`

Errors: `401 Missing or invalid JWT`, `403 Missing operator role`, `404 Chat not found`, `409 Chat is not in WAITING status`

### `POST /operator/chats/{chatId}/message`

Adds an operator reply to an active chat. Only the operator who acquired the
chat can send messages to it.

Auth: Bearer JWT with role `operator`

Path parameter: `chatId`

Request body:

```json
{
  "message": "Can you restart the router and tell me if the lights change?"
}
```

Success: `200 OK` with a `Message` response

Errors: `401 Missing or invalid JWT`, `403 Missing operator role or wrong assigned operator`, `404 Chat not found`, `409 Chat is not ACTIVE`

### `GET /operator/chats/{chatId}/messages`

Returns the full message history for a chat. Only the operator who acquired the
chat can view it.

Auth: Bearer JWT with role `operator`

Path parameter: `chatId`

Success: `200 OK` with `Message[]`

Errors: `401 Missing or invalid JWT`, `403 Missing operator role or wrong assigned operator`, `404 Chat not found`

## Shared Response Shapes

### `Chat`

Returned by `POST /user/chat`.

```json
{
  "idChat": 5,
  "version": 0,
  "user": {
    "idUser": 1,
    "username": "ana",
    "createdAt": "2026-04-16T10:00:00"
  },
  "operator": null,
  "room": "TEHNIKA",
  "status": "WAITING",
  "createdAt": "2026-04-16T10:15:30",
  "acquiredAt": null
}
```

### `Message`

Returned by both user and operator message endpoints.

```json
{
  "idMessage": 12,
  "senderType": "USER",
  "senderId": 1,
  "content": "The connection dropped again.",
  "timeSent": "2026-04-16T10:17:00",
  "chatId": 5
}
```

### `ChatSummaryDTO`

Returned by `GET /operator/chats`.

```json
{
  "idChat": 1,
  "status": "WAITING",
  "createdAt": "2026-04-16T10:15:30"
}
```

### `ChatDetailDTO`

Returned by `POST /operator/chats/{chatId}/acquire`.

```json
{
  "idChat": 4,
  "status": "ACTIVE",
  "room": "TEHNIKA",
  "username": "ana",
  "createdAt": "2026-04-16T10:15:30",
  "acquiredAt": "2026-04-16T10:16:02",
  "messages": [
    {
      "idMessage": 1,
      "senderType": "USER",
      "senderId": 1,
      "content": "Hi, my internet has been dropping every 10 minutes since this morning.",
      "timeSent": "2026-04-16T10:06:00",
      "chatId": 4
    }
  ]
}
```
