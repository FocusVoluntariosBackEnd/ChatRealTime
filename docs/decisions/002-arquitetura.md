
# 002 - Arquitetura

## Decisão

O sistema adota um monólito modular em Spring Boot, organizado por domínio: `auth`, `user`, `conversation`, `message` e `notification`.

Cada módulo separa entrada, regras de negócio e persistência em `controller`, `service`, `repository`, `entity` e `dto`. Configurações transversais ficam em `security`, `websocket`, `config` e `exception`.

## Responsabilidades

- **Controllers HTTP e WebSocket:** recebem requisições e eventos, extraem o usuário autenticado, validam o payload e delegam ao caso de uso.
- **Services:** concentram regras de negócio, autorização, transações e coordenação entre repositórios.
- **Repositories:** encapsulam o acesso ao PostgreSQL via Spring Data JPA.
- **Entities:** representam o modelo persistente e seus relacionamentos.
- **DTOs:** definem contratos de entrada e saída da API HTTP e dos eventos WebSocket.
- **Security:** protege as rotas HTTP com JWT e configura autenticação sem sessão.
- **WebSocket:** expõe `/ws`, usa STOMP, autentica a conexão e valida a assinatura de tópicos de conversa.
- **Exception handler:** converte exceções de regra de negócio em respostas HTTP padronizadas.

## Motivação e trade-offs

A separação por domínio mantém funcionalidades relacionadas próximas entre si e evita concentrar o sistema em camadas genéricas. A camada de serviço permite reutilizar regras entre HTTP e WebSocket, especialmente as de participação, autorização e persistência.

Microsserviços não foram adotados neste estágio, pois aumentariam a complexidade de deploy, operação e comunicação sem benefício proporcional ao tamanho atual do sistema. A arquitetura facilita testes e manutenção, mas o broker simples do Spring é local à instância; Redis será necessário para distribuição de eventos entre instâncias.
