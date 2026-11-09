
# 009 - Paginação e recuperação de histórico

## Decisão

Usar a API HTTP como fonte de recuperação do histórico e o WebSocket apenas para atualização em tempo real. As mensagens permanecem no PostgreSQL; assim, uma queda de conexão ou indisponibilidade temporária do Redis não causa perda do histórico.

## Paginação

Foi escolhida paginação por página e limite, retornando `Slice` e `nextPage`, em vez de totalizar registros. A consulta ordena por `createdAt` decrescente e, quando há empate, por `id` decrescente. Esse segundo critério torna a ordem determinística e evita que duas mensagens com o mesmo instante tenham ordem ambígua.

O limite máximo de 100 reduz o risco de consultas muito grandes. A opção por `Slice` evita uma consulta extra de contagem, que não é necessária para o cliente recuperar blocos sucessivos do histórico.

## Autorização e recuperação

Antes de consultar as mensagens, a associação do usuário à conversa é verificada. Para conversas não acessíveis, a resposta é de recurso não encontrado, preservando a existência e a composição de conversas privadas.

Após reconectar, o cliente deve buscar o histórico pela API e reconciliar as mensagens já recebidas. Essa decisão não trata Redis Pub/Sub como mecanismo de entrega garantida: ele é efêmero por natureza, enquanto o banco mantém a fonte durável.

## Alternativas e impactos

Cursor por mensagem seria mais robusto quando novas mensagens chegam durante a navegação. A paginação por página foi escolhida por simplicidade e por atender ao contrato atual. Como limite, páginas podem se deslocar se novas mensagens forem inseridas entre duas requisições; não há snapshot nem cursor estável entre consultas.
