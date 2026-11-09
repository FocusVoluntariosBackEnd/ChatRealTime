
# 011 - Tratamento de erros e segurança

## Decisão

Centralizar erros HTTP em `RestControllerAdvice`, validar dados recebidos nas bordas e aplicar autorização em todas as operações que acessam recursos privados. A segurança não confia em IDs fornecidos pelo cliente: o usuário é obtido do JWT e a associação à conversa é verificada no servidor.

## Política de respostas HTTP

Erros de validação retornam `400`; credenciais inválidas retornam `401`; conflitos de regra ou integridade retornam `409`. Para recursos privados que o usuário não pode consultar, a aplicação usa `404`, evitando confirmar sua existência. Operações identificadas como proibidas retornam `403`.

O uso de `ProblemDetail` nos erros de domínio fornece status, título, detalhe e instante. Erros de validação de campos retornam o mapeamento de campo e mensagem, para que o cliente possa corrigir a entrada sem depender de mensagens internas do servidor.

## Validação e autorização no WebSocket

O token JWT é validado no comando STOMP `CONNECT`; assinaturas inválidas, emissor incorreto ou expiração impedem a conexão. A inscrição em tópicos de conversa valida se o usuário é membro. Os casos de uso de envio, leitura e consulta repetem a verificação de associação, para que um destino WebSocket não substitua a autorização de domínio.

## Segredos e exposição de dados

A chave JWT vem de variável de ambiente, deve ser Base64 e ter ao menos 32 bytes. Ela não deve ser incluída em código, commits ou documentação. A política de `404` para recursos privados e o uso do principal autenticado reduzem enumeração de conversas, notificações e mensagens de terceiros.

## Alternativas e impactos

Uma camada única de erro para HTTP e STOMP poderia padronizar todos os formatos, mas exige tratamento específico dos canais de mensageria. A separação atual mantém o tratamento REST simples, porém torna necessária a evolução explícita do tratamento de erros WebSocket.
