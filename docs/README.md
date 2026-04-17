# Kaldi Support API Reference

Static reference for `kaldi-support-api`.

Use this document when you want to review the contract without starting the
project. If the API is running, the generated Swagger UI is available at
`http://localhost:8080/swagger-ui`.

## Quick Reference

| Item | Value |
|------|-------|
| Base URL | `http://localhost:8080` |
| Content type | `application/json` |
| Anonymous endpoints | `/user/*` |
| Protected endpoints | `/operator/*` |
| Live generated docs | `http://localhost:8080/swagger-ui` |

## Authentication Model

| Area | Auth | Identity source | Notes |
|------|------|-----------------|-------|
| `/user/*` | None | `userId` in the request body or the existing chat relation | Intended for the mobile user flow |
| `/operator/*` | Bearer JWT | `preferred_username` claim from Keycloak | Token must include the `operator` role |

Operator authentication is handled by Keycloak. This service does not expose a
backend `/auth` endpoint.

## Dev Operator Accounts

| Username | Password | Role |
|----------|----------|------|
| `aliceOperator` | `alice` | `operator` |
| `mikeOperator` | `mike` | `operator` |
| `lucyOperator` | `lucy` | `operator` |

## Common Behavior

- All request and response bodies are JSON.
- Timestamps are returned as ISO-8601 local date-times such as `2026-04-16T10:15:30`.
- Validation failures return `400 Bad Request`.
- Protected endpoints return `401 Unauthorized` for a missing or invalid token.
- Protected endpoints return `403 Forbidden` when the token is valid but the caller is not allowed to perform the action.

## Domain Values

| Enum | Values |
|------|--------|
| `Room` | `TEHNIKA`, `STORITVE`, `POGOVOR` |
| `ChatStatus` | `WAITING`, `ACTIVE`, `CLOSED` |
| `SenderType` | `USER`, `OPERATOR` |

## Endpoint Index

| Method | Path | Auth | Returns | Purpose |
|--------|------|------|---------|---------|
| `POST` | `/user/chat` | None | `Chat` | Create a new waiting chat and store the first user message |
| `POST` | `/user/chat/{chatId}/message` | None | `Message` | Add a user message to an existing chat |
| `GET` | `/user/chat/{chatId}/messages` | None | `Message[]` | Fetch all messages in a chat |
| `GET` | `/operator/chats` | Bearer JWT | `ChatSummaryDTO[]` | List all open chats |
| `POST` | `/operator/chats/{chatId}/acquire` | Bearer JWT | `ChatDetailDTO` | Assign a waiting chat to the authenticated operator |
| `POST` | `/operator/chats/{chatId}/message` | Bearer JWT | `Message` | Add an operator reply to an active chat |
| `GET` | `/operator/chats/{chatId}/messages` | Bearer JWT | `Message[]` | Fetch the full message history for an acquired chat |

## User Endpoints

### `POST /user/chat`

Creates a new `WAITING` chat and stores the first user message.

| Item | Value |
|------|-------|
| Auth | None |
| Request body | `userId`, `room`, `message` |
| Success | `200 OK` with `Chat` |
| Errors | `400 Bad Request`, `404 User not found` |

Request body example:

```json
{
  "userId": 1,
  "room": "TEHNIKA",
  "message": "Hi, my router keeps disconnecting."
}
```

### `POST /user/chat/{chatId}/message`

Adds a new user message to an existing chat.

| Item | Value |
|------|-------|
| Auth | None |
| Path parameter | `chatId` |
| Request body | `message` |
| Success | `200 OK` with `Message` |
| Errors | `400 Bad Request`, `404 Chat not found` |

Request body example:

```json
{
  "message": "The connection dropped again."
}
```

### `GET /user/chat/{chatId}/messages`

Returns every message in the chat ordered from oldest to newest.

| Item | Value |
|------|-------|
| Auth | None |
| Path parameter | `chatId` |
| Success | `200 OK` with `Message[]` |
| Errors | `404 Chat not found` |

## Operator Endpoints

### `GET /operator/chats`

Returns every open chat shown in the operator dashboard. The list includes chats
in `WAITING` and `ACTIVE` status.

| Item | Value |
|------|-------|
| Auth | Bearer JWT with role `operator` |
| Success | `200 OK` with `ChatSummaryDTO[]` |
| Errors | `401 Unauthorized`, `403 Forbidden` |

### `POST /operator/chats/{chatId}/acquire`

Assigns a waiting chat to the authenticated operator and moves it to `ACTIVE`.

| Item | Value |
|------|-------|
| Auth | Bearer JWT with role `operator` |
| Path parameter | `chatId` |
| Success | `200 OK` with `ChatDetailDTO` |
| Errors | `401 Unauthorized`, `403 Forbidden`, `404 Chat not found`, `409 Chat is not in WAITING status` |

### `POST /operator/chats/{chatId}/message`

Adds an operator reply to an active chat. Only the operator who acquired the
chat can send messages to it.

| Item | Value |
|------|-------|
| Auth | Bearer JWT with role `operator` |
| Path parameter | `chatId` |
| Request body | `message` |
| Success | `200 OK` with `Message` |
| Errors | `400 Bad Request`, `401 Unauthorized`, `403 Forbidden`, `404 Chat not found`, `409 Chat is not ACTIVE` |

Request body example:

```json
{
  "message": "Can you restart the router and tell me if the lights change?"
}
```

### `GET /operator/chats/{chatId}/messages`

Returns the full message history for a chat. Only the operator who acquired the
chat can view it.

| Item | Value |
|------|-------|
| Auth | Bearer JWT with role `operator` |
| Path parameter | `chatId` |
| Success | `200 OK` with `Message[]` |
| Errors | `401 Unauthorized`, `403 Forbidden`, `404 Chat not found` |

## Response Shapes

### `Chat`

Returned by `POST /user/chat`.

| Field | Type | Notes |
|-------|------|-------|
| `idChat` | `number` | Chat identifier |
| `version` | `number` | Optimistic-lock version |
| `user` | `object` | Full user entity |
| `operator` | `object \| null` | `null` until the chat is acquired |
| `room` | `Room` | Support category |
| `status` | `ChatStatus` | Initial value is `WAITING` |
| `createdAt` | `string` | Chat creation timestamp |
| `acquiredAt` | `string \| null` | Set when an operator acquires the chat |

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

| Field | Type | Notes |
|-------|------|-------|
| `idMessage` | `number` | Message identifier |
| `senderType` | `SenderType` | `USER` or `OPERATOR` |
| `senderId` | `number` | User or operator ID |
| `content` | `string` | Message body |
| `timeSent` | `string` | Message timestamp |
| `chatId` | `number` | Owning chat ID |

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

| Field | Type | Notes |
|-------|------|-------|
| `idChat` | `number` | Chat identifier |
| `status` | `ChatStatus` | Open chat status |
| `createdAt` | `string` | Chat creation timestamp |

```json
{
  "idChat": 1,
  "status": "WAITING",
  "createdAt": "2026-04-16T10:15:30"
}
```

### `ChatDetailDTO`

Returned by `POST /operator/chats/{chatId}/acquire`.

| Field | Type | Notes |
|-------|------|-------|
| `idChat` | `number` | Chat identifier |
| `status` | `ChatStatus` | Current chat status |
| `room` | `Room` | Support category |
| `username` | `string` | Username of the customer who opened the chat |
| `createdAt` | `string` | Chat creation timestamp |
| `acquiredAt` | `string \| null` | Acquisition timestamp |
| `messages` | `Message[]` | Full message history |

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
