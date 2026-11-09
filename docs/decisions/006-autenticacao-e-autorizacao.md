
# 006 - Autenticação e autorização

## Decisão

Foi adotada autenticação stateless com JWT Bearer. O token usa HS256, contém o UUID do usuário no campo `sub`, possui emissor configurado e expiração de curta duração.

JWT foi escolhido em vez de sessões armazenadas no servidor para que as requisições HTTP e conexões WebSocket possam ser autenticadas sem estado compartilhado entre instâncias.

## Credenciais e segredos

Senhas são armazenadas exclusivamente como hash BCrypt e nunca são retornadas nas respostas. A chave de assinatura JWT é Base64, deve possuir ao menos 32 bytes e é carregada pela variável `JWT_SECRET`.

O arquivo `.env` real permanece fora do Git; `.env.example` expõe apenas os nomes das variáveis necessárias.

## Autorização HTTP

Somente cadastro e login são públicos. As demais rotas HTTP exigem JWT válido pelo Resource Server do Spring Security.

As regras de acesso são aplicadas nos casos de uso, com base no usuário autenticado: participação em conversas, papel de administrador em grupos e propriedade de notificações ou leituras. Identificadores informados pelo cliente não são usados como prova de autorização.

Operações de alteração sem permissão retornam `403`. Consultas a recursos privados inacessíveis retornam `404` em fluxos de mensagens e notificações, reduzindo a exposição da existência desses recursos.

## Autenticação e autorização WebSocket

O cliente envia o token Bearer no comando STOMP `CONNECT`. O interceptor valida assinatura, emissor e expiração, e registra o usuário autenticado como principal da sessão.

Assinaturas em tópicos de conversa são autorizadas pela participação atual do usuário. O envio e a leitura de mensagens repetem a validação de participação na camada de serviço.

## Alternativas e trade-offs

Sessões armazenadas no servidor poderiam permitir revogação centralizada imediata, mas exigiriam armazenamento e sincronização entre instâncias. JWT reduz essa dependência, com o custo de depender da expiração do token para revogação.

HS256 simplifica a configuração ao usar uma chave compartilhada. Como consequência, todas as instâncias precisam proteger e compartilhar o mesmo segredo com segurança.

## Limitação atual

Erros HTTP seguem respostas estruturadas do Spring. O contrato de erro estruturado para eventos WebSocket ainda deve ser implementado para atender completamente ao requisito de comunicação de falhas em tempo real.
