
# 004 - WebSocket

## Decisão

Foi adotado WebSocket nativo com STOMP, utilizando o Spring WebSocket. O STOMP foi escolhido para organizar comandos, tópicos e filas privadas sem criar um protocolo próprio de mensageria.

## Destinos e eventos

A aplicação separa comandos enviados ao servidor, eventos de conversa e eventos privados:

- `/app`: comandos recebidos pelo backend.
- `/topic/conversations/{conversationId}`: eventos destinados aos participantes de uma conversa.
- `/user/queue/...`: eventos privados, como ACKs e notificações.

Os payloads usam o envelope `event` e `data`. Essa escolha padroniza os eventos, permite validação do contrato e facilita a inclusão de novos tipos sem alterar a estrutura geral da comunicação.

## Autenticação e autorização

O JWT é enviado no `CONNECT` e define o usuário autenticado da sessão STOMP. Assim, o backend não confia em identificadores de usuário enviados pelo cliente.

A assinatura de tópicos de conversa verifica se o usuário é membro da conversa. O envio e a leitura de mensagens também validam a participação no caso de uso.

## Alternativas e trade-offs

Socket.IO poderia oferecer recursos adicionais de fallback e reconexão, mas adicionaria um protocolo específico. STOMP sobre WebSocket foi escolhido por ser suficiente para comandos, tópicos e filas privadas no escopo atual.

Tópicos por conversa simplificam a distribuição para vários participantes, mas exigem autorização na assinatura e cuidado com a revogação de acesso de membros removidos.

A estratégia de presença e heartbeat é documentada separadamente na decisão 007, pois envolve ciclo de vida de sessões e estado temporário no Redis.
