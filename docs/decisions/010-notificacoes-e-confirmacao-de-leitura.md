
# 010 - Notificações e confirmação de leitura

## Decisão

Persistir notificações e confirmações de leitura no PostgreSQL e usar WebSocket apenas para avisar usuários conectados. A persistência foi escolhida para que notificações não desapareçam quando o destinatário está offline e para que o estado de leitura seja auditável e recuperável.

## Notificações

Ao enviar uma mensagem, é criada uma notificação para cada membro da conversa, exceto o remetente. A unicidade por `(user_id, message_id)` impede duplicidade persistida. A notificação é publicada somente após o commit e é direcionada à fila privada do destinatário; entre instâncias, Redis Pub/Sub distribui o aviso.

O usuário consulta e marca como lidas apenas as próprias notificações. Essa escolha mantém o isolamento por destinatário e permite recuperar notificações não vistas pela API, independentemente de o evento em tempo real ter sido entregue.

## Confirmação de leitura

O estado de leitura é modelado separadamente por mensagem e usuário, em vez de alterar a própria mensagem. Isso permite que cada participante tenha seu próprio estado. A unicidade `(message_id, user_id)` torna a ação idempotente no armazenamento; após confirmação, o evento de atualização é distribuído para a conversa somente depois do commit.

## Alternativas e impactos

Seria possível manter notificações e leituras apenas em memória ou somente no WebSocket. Essa alternativa reduziria gravações, mas perderia estado de usuários offline e não permitiria recuperação confiável. O custo da decisão é mais escrita no banco e a necessidade de tratar a disputa entre duas confirmações simultâneas, descrita na decisão 008.
