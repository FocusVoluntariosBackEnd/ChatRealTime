
# 005 - Redis e distribuição entre instâncias

## Decisão

Redis foi adotado com Pub/Sub para distribuir eventos em tempo real entre instâncias do backend. O PostgreSQL permanece como fonte de verdade para mensagens, leituras e notificações.

## Organização dos canais

Eventos de mensagens e leitura usam canais por conversa (`chat:conversations:{conversationId}`). Notificações usam canais por usuário (`chat:users:{userId}:notifications`). Essa separação reduz a entrega de eventos irrelevantes e acompanha o escopo de autorização de cada evento.

Cada instância assina os padrões de canais necessários e entrega localmente os eventos Redis aos seus clientes WebSocket conectados.

## Consistência e recuperação

Os eventos são publicados somente após o commit da transação. Assim, um evento de mensagem, leitura ou notificação não é distribuído se sua persistência falhar.

Redis Pub/Sub não armazena eventos para clientes ou instâncias desconectadas. A recuperação ocorre pelo histórico de mensagens e pelas notificações persistidas no PostgreSQL. Para mensagens, o `clientMessageId` evita uma nova publicação quando o mesmo envio já foi persistido.

## Presença

Redis também é a tecnologia escolhida para manter estado temporário de presença e sessões. A estratégia de conexões múltiplas, expiração e heartbeat é detalhada na decisão 007.

## Alternativas e trade-offs

Redis Streams ou um broker dedicado poderiam oferecer retenção e reprocessamento de eventos. Redis Pub/Sub foi escolhido por ser mais simples para entrega imediata entre instâncias e porque o sistema já possui recuperação por dados persistidos.

Como limitação, Pub/Sub não garante entrega para assinantes desconectados. Por isso, eventos em tempo real não substituem o histórico e as notificações persistidas.
