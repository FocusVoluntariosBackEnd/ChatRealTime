
# 003 - Modelagem de dados

## Decisão

O modelo relacional utiliza PostgreSQL com UUIDs como identificadores e datas em UTC.

As entidades principais são:

- **User:** usuário autenticado, com e-mail único e senha armazenada como hash.
- **Conversation:** representa conversas `PRIVATE` ou `GROUP`.
- **ConversationMember:** relaciona usuários e conversas, registrando o papel `ADMIN` ou `MEMBER`.
- **Message:** pertence a uma conversa e a um remetente; armazena conteúdo, data e `clientMessageId`.
- **MessageRead:** registra a leitura de uma mensagem por um usuário.
- **Notification:** associa uma mensagem a um destinatário e registra seu estado de leitura.

## Integridade e consistência

- O e-mail é único.
- Um usuário só pode participar uma vez da mesma conversa.
- Conversas privadas armazenam o par ordenado de usuários (`userA` e `userB`), com índice único para impedir duplicação.
- Mensagens são vinculadas à conversa e ao remetente.
- `clientMessageId` é único por conversa para apoiar idempotência de reenvios.
- Leituras e notificações são únicas para cada par mensagem/usuário.
- Há índices para consulta de mensagens por conversa/data, conversas por usuário e notificações por usuário.

## Motivação

`ConversationMember` permite que grupos tenham qualquer quantidade de membros e que cada participante possua um papel, sem duplicar dados da conversa.

O par ordenado em conversas privadas garante, no banco, que a mesma dupla não gere conversas duplicadas mesmo em solicitações simultâneas.

`MessageRead` e `Notification` foram modeladas separadamente para manter histórico de leitura e notificações por destinatário, sem alterar a mensagem original. As constraints e índices preservam consistência e atendem às consultas mais frequentes.
