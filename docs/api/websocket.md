
# API WebSocket

## Conexão e autenticação

O endpoint STOMP é `/ws`. A conexão deve enviar o JWT no comando `CONNECT`:

```text
CONNECT
Authorization: Bearer <accessToken>
```

Comandos enviados pelo cliente usam o prefixo `/app`. Assinaturas de conversa usam `/topic/conversations/{conversationId}`. Filas privadas usam o prefixo `/user/queue`.

Todos os eventos usam o envelope:

```json
{
  "event": "nome-do-evento",
  "data": {}
}
```

## Envio e confirmação de mensagem

O cliente envia para `/app/messages.send`:

```json
{
  "event": "message:send",
  "data": {
    "conversationId": "22222222-2222-2222-2222-222222222222",
    "clientMessageId": "msg-001",
    "content": "Olá"
  }
}
```

O remetente recebe em `/user/queue/messages.ack`:

```json
{
  "event": "message:ack",
  "data": {
    "clientMessageId": "msg-001",
    "messageId": "33333333-3333-3333-3333-333333333333",
    "status": "SAVED"
  }
}
```

`SAVED` confirma a persistência. O mesmo `clientMessageId` deve ser usado em um reenvio para evitar duplicação.

Participantes inscritos em `/topic/conversations/{conversationId}` recebem:

```json
{
  "event": "message:new",
  "data": {
    "id": "33333333-3333-3333-3333-333333333333",
    "conversationId": "22222222-2222-2222-2222-222222222222",
    "senderId": "11111111-1111-1111-1111-111111111111",
    "senderName": "Ana",
    "content": "Olá",
    "createdAt": "2026-01-01T10:00:00Z"
  }
}
```

## Leitura, notificações e presença

Leitura é enviada a `/app/messages.read`:

```json
{
  "event": "message:read",
  "data": {
    "conversationId": "22222222-2222-2222-2222-222222222222",
    "messageId": "33333333-3333-3333-3333-333333333333"
  }
}
```

Atualizações de leitura são recebidas na conversa:

```json
{
  "event": "message:read:update",
  "data": {
    "conversationId": "22222222-2222-2222-2222-222222222222",
    "messageId": "33333333-3333-3333-3333-333333333333",
    "userId": "11111111-1111-1111-1111-111111111111",
    "readAt": "2026-01-01T10:01:00Z"
  }
}
```

Notificações são recebidas em `/user/queue/notifications`:

```json
{
  "event": "notification:new",
  "data": {
    "id": "44444444-4444-4444-4444-444444444444",
    "messageId": "33333333-3333-3333-3333-333333333333",
    "createdAt": "2026-01-01T10:00:00Z"
  }
}
```

Presença é recebida em `/user/queue/presence` apenas por usuários que compartilham conversa com o usuário informado:

```json
{
  "event": "user:presence",
  "data": {
    "userId": "11111111-1111-1111-1111-111111111111",
    "status": "ONLINE"
  }
}
```

`status` pode ser `ONLINE` ou `OFFLINE`.

## Erros e reconexão

Falhas de evento são recebidas na fila privada `/user/queue/errors`:

```json
{
  "event": "error",
  "data": {
    "code": "VALIDATION_ERROR",
    "message": "Dados do evento inválidos"
  }
}
```

Após reconectar, o cliente consulta o histórico HTTP para recuperar mensagens que não recebeu durante a desconexão. Assinaturas e comandos só são autorizados para participantes da conversa.
