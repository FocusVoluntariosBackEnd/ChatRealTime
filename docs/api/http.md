
# API HTTP

## Autenticação

Exceto cadastro e login, as rotas exigem o cabeçalho:

```http
Authorization: Bearer <accessToken>
```

### Cadastro

```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "Ana",
  "email": "ana@example.com",
  "password": "senha-segura"
}
```

Retorna `201 Created` com os dados do usuário, sem a senha.

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "ana@example.com",
  "password": "senha-segura"
}
```

```json
{
  "accessToken": "<jwt>",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

## Conversas

| Método | Rota | Finalidade |
| --- | --- | --- |
| `POST` | `/api/conversations/private` | Cria ou retorna uma conversa privada. |
| `GET` | `/api/conversations` | Lista as conversas do usuário autenticado. |
| `POST` | `/api/conversations/groups` | Cria um grupo. |
| `POST` | `/api/conversations/groups/{id}/members` | Adiciona membro ao grupo; requer administrador. |
| `DELETE` | `/api/conversations/groups/{id}/members/{userId}` | Remove membro do grupo; requer administrador. |

Exemplo de conversa privada:

```json
{
  "participantId": "11111111-1111-1111-1111-111111111111"
}
```

Exemplo de criação de grupo:

```json
{
  "name": "Projeto",
  "membersIds": [
    "11111111-1111-1111-1111-111111111111"
  ]
}
```

## Histórico de mensagens

```http
GET /api/conversations/{conversationId}/messages?page=1&limit=20
```

Somente participantes podem consultar o histórico. As mensagens são retornadas da mais recente para a mais antiga. `page` inicia em `1` e `limit` aceita de `1` a `100`.

```json
{
  "messages": [
    {
      "id": "22222222-2222-2222-2222-222222222222",
      "senderId": "11111111-1111-1111-1111-111111111111",
      "content": "Olá",
      "createdAt": "2026-01-01T10:00:00Z"
    }
  ],
  "nextPage": 2
}
```

`nextPage` é `null` quando não há mais resultados. O histórico é a forma de recuperar mensagens após uma reconexão.

## Confirmação de leitura e notificações

| Método | Rota | Finalidade |
| --- | --- | --- |
| `PATCH` | `/api/messages/{id}/read` | Registra a leitura de uma mensagem acessível ao usuário. |
| `GET` | `/api/notifications?page=1&limit=20` | Lista notificações do usuário autenticado. |
| `PATCH` | `/api/notifications/{id}/read` | Marca uma notificação própria como lida. |

Notificações usam a mesma paginação do histórico e retornam `notifications` e `nextPage`.

## Respostas de erro

| Status | Significado |
| --- | --- |
| `400` | Dados ou paginação inválidos. |
| `401` | Token ausente, inválido ou expirado. |
| `403` | Ação sem a permissão necessária, como administrar grupo. |
| `404` | Recurso inexistente ou não acessível ao usuário. |
| `409` | Conflito de regra de negócio ou integridade. |
