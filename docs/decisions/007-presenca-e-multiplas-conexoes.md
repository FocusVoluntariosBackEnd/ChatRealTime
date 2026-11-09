
# 007 - Presença e múltiplas conexões

## Decisão

O estado de presença é temporário e armazenado no Redis, não no banco relacional. Um usuário é considerado `ONLINE` enquanto possuir ao menos uma sessão WebSocket ativa; torna-se `OFFLINE` apenas quando não restar nenhuma sessão válida.

Essa decisão atende conexões simultâneas em abas, navegadores ou dispositivos diferentes sem marcar o usuário como offline quando somente uma conexão é encerrada.

## Sessões, expiração e heartbeat

Cada sessão possui uma chave no Redis com TTL de 60 segundos. As sessões de um usuário são agrupadas em um conjunto próprio e os usuários online em outro conjunto.

O servidor configura heartbeat STOMP de 15 segundos e executa uma renovação e limpeza de sessões a cada 20 segundos. Sessões desconectadas ou expiradas são removidas antes de decidir a transição para `OFFLINE`.

## Distribuição e autorização

Alterações reais de estado são publicadas no canal Redis `chat:presence:events` como eventos `user:presence` com `userId` e status `ONLINE` ou `OFFLINE`.

O evento é entregue na fila privada `/user/queue/presence` apenas para usuários conectados que compartilham uma conversa com o usuário cujo status mudou. A participação é consultada novamente no momento da entrega, evitando que membros removidos continuem recebendo atualizações.

## Alternativas e trade-offs

Manter presença apenas em memória seria mais simples, mas não funcionaria com múltiplas instâncias. Persistir presença no PostgreSQL criaria escrita constante para um dado efêmero.

Redis foi escolhido por oferecer TTL, conjuntos compartilhados e Pub/Sub. Como trade-off, uma queda abrupta pode manter o status ativo até a expiração do TTL; o heartbeat e a limpeza periódica limitam esse intervalo.
