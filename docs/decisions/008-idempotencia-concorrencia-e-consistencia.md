
# 008 - Idempotência, concorrência e consistência

## Decisão

Usar transações de banco, restrições de unicidade e ordenação determinística como garantias de consistência. O PostgreSQL é a fonte de verdade; Redis só recebe eventos depois da confirmação da transação.

## Reenvio de mensagens

Cada mensagem recebe `clientMessageId`. Antes de criar uma nova mensagem, a aplicação procura o registro pela combinação de conversa e identificador do cliente. A tabela também possui unicidade para `(conversation_id, client_message_id)`, impedindo duplicidade persistida caso ocorram tentativas concorrentes.

O ACK retornado representa a mensagem já persistida. Assim, em uma reconexão sem confirmação anterior, o cliente pode repetir o mesmo `clientMessageId` sem criar outra mensagem.

## Conversas privadas e confirmações de leitura

Para conversas privadas, os usuários são ordenados em `user_a_id` e `user_b_id`; um índice único para o par impede a criação de duas conversas entre os mesmos participantes. A criação tenta recuperar a conversa já existente após uma violação de unicidade.

As confirmações de leitura usam a chave única `(message_id, user_id)`. A verificação prévia evita gravação repetida no fluxo comum, e a restrição no banco preserva a consistência quando há concorrência.

## Histórico e publicação de eventos

O histórico é ordenado por `createdAt` e, em empate, por `id`, produzindo uma paginação estável. Operações de envio e leitura são transacionais. Os eventos de mensagem e leitura só são publicados no Redis no estágio `AFTER_COMMIT`, evitando distribuir eventos de uma transação que não foi confirmada.

## Limites conhecidos

As verificações prévias de mensagem e leitura ainda têm uma janela entre consultar e inserir. Em dois reenvios ou confirmações simultâneos, a restrição do banco impede o dado duplicado, mas a exceção de unicidade ainda não é convertida em uma resposta idempotente. Esse tratamento deve recuperar o registro existente e devolver o ACK ou encerrar a operação de leitura com sucesso.

## Alternativas e impactos

Poderiam ser usados locks explícitos ou serialização de transações. A escolha por restrições de banco mantém a proteção centralizada e simples, com menor contenção. Em contrapartida, os fluxos que sofrem disputa precisam tratar explicitamente a violação de unicidade para oferecer uma experiência idempotente ao cliente.
