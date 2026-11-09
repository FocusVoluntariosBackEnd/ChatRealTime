# Chat Real Time

O Chat Real Time é uma API de mensageria em tempo real para comunicação entre usuários, com suporte a conversas privadas e grupos.

O projeto resolve os principais desafios de um chat moderno: manter o histórico confiável, entregar atualizações em tempo real, notificar usuários que estão offline, identificar presença mesmo quando o usuário possui várias abas ou dispositivos conectados e manter a comunicação funcionando em mais de uma instância da aplicação.

As mensagens, conversas, leituras e notificações são persistidas no PostgreSQL. O WebSocket/STOMP permite a comunicação imediata entre clientes conectados. O Redis é usado para distribuir eventos entre instâncias e manter o estado temporário de presença.

## Funcionalidades

- Cadastro e autenticação de usuários com JWT.
- Criação de conversas privadas sem duplicar pares de usuários.
- Criação e administração de grupos.
- Envio de mensagens em tempo real.
- Confirmação de persistência da mensagem por ACK.
- Proteção contra reenvio duplicado usando `clientMessageId`.
- Histórico paginado de mensagens.
- Confirmação de leitura por usuário.
- Notificações persistidas para destinatários.
- Presença `ONLINE` e `OFFLINE`, considerando múltiplas conexões do mesmo usuário.
- Distribuição de eventos entre múltiplas instâncias com Redis Pub/Sub.
- Recuperação de mensagens e notificações persistidas após reconexão.

## Como funciona

1. O usuário se cadastra e realiza login para receber um JWT.
2. Com o token, cria ou acessa conversas privadas e grupos.
3. O cliente abre uma conexão WebSocket/STOMP autenticada.
4. Ao enviar uma mensagem, a aplicação valida o participante, persiste a mensagem e retorna um ACK ao remetente.
5. Após a confirmação da transação, a mensagem é distribuída aos participantes conectados.
6. Destinatários recebem notificações e podem confirmar a leitura.
7. Se uma conexão cair, o cliente consulta o histórico HTTP para recuperar o que não recebeu em tempo real.
8. Em múltiplas instâncias, Redis distribui os eventos e mantém o estado temporário de presença.

## Stack

- Java 25 e Spring Boot
- Spring Security com JWT
- Spring Data JPA e Flyway
- PostgreSQL
- WebSocket com STOMP
- Redis
- Docker Compose
- Swagger / OpenAPI

## Pré-requisitos

- Java 25
- Docker e Docker Compose

## Configuração rápida

Crie o arquivo `.env` a partir de `.env.example` e informe as variáveis necessárias.

```bash
docker compose up -d
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Veja as variáveis e instruções completas em [Configuração e execução](docs/operations/configuracao-e-execucao.md).

A aplicação fica disponível em `http://localhost:8080`.

## Testes

```bash
./mvnw test
```

Os fluxos HTTP podem ser validados pelo Swagger e os fluxos em tempo real pelo frontend de exemplo. Consulte a [Estratégia e execução de testes](docs/testing/estrategia-e-execucao.md) e o [roteiro de teste pelo FrontTeste](docs/testing/teste-pelo-frontTest.md).

## Documentação

### Decisões técnicas

- [001 - Escolha da stack](docs/decisions/001-escolha-da-stack.md)
- [002 - Arquitetura](docs/decisions/002-arquitetura.md)
- [003 - Modelagem de dados](docs/decisions/003-modelagem-de-dados.md)
- [004 - WebSocket](docs/decisions/004-websocket.md)
- [005 - Redis e distribuição entre instâncias](docs/decisions/005-redis.md)
- [006 - Autenticação e autorização](docs/decisions/006-autenticacao-e-autorizacao.md)
- [007 - Presença e múltiplas conexões](docs/decisions/007-presenca-e-multiplas-conexoes.md)
- [008 - Idempotência, concorrência e consistência](docs/decisions/008-idempotencia-concorrencia-e-consistencia.md)
- [009 - Paginação e recuperação de histórico](docs/decisions/009-paginacao-e-recuperacao-de-historico.md)
- [010 - Notificações e confirmação de leitura](docs/decisions/010-notificacoes-e-confirmacao-de-leitura.md)
- [011 - Tratamento de erros e segurança](docs/decisions/011-tratamento-de-erros-e-seguranca.md)

### Contratos de API

- [API HTTP](docs/api/http.md)
- [API WebSocket](docs/api/websocket.md)

### Operação

- [Configuração e execução](docs/operations/configuracao-e-execucao.md)
- [Múltiplas instâncias](docs/operations/escalabilidade-e-multiplas-instancias.md)
- [Limitações conhecidas](docs/operations/limitacoes-conhecidas.md)

### Testes

- [Estratégia e execução de testes](docs/testing/estrategia-e-execucao.md)
- [Teste pelo FrontTeste](docs/testing/teste-pelo-frontTest.md)
