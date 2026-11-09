
# 001 - Escolha da stack

## Decisão

O backend foi desenvolvido em **Java 25** com **Spring Boot 4.1.1** e Maven. A aplicação usa Spring MVC para a API HTTP, Spring WebSocket/STOMP para comunicação em tempo real, Spring Security com JWT para autenticação, JPA/Hibernate para persistência e Flyway para versionar o esquema do banco.

O banco escolhido é o **PostgreSQL 17**, executado localmente por Docker Compose. OpenAPI/Swagger é disponibilizado com Springdoc e os testes utilizam JUnit e os módulos de teste do Spring Boot.

## Motivação e trade-offs

Spring Boot fornece integração madura entre API, segurança, validação, transações, WebSocket e persistência, reduzindo código de infraestrutura. PostgreSQL oferece transações, chaves estrangeiras e restrições de unicidade necessárias para conversas, mensagens e idempotência. Flyway mantém as alterações do banco reproduzíveis e versionadas.

Como alternativa, poderiam ser usados Node.js/NestJS ou Python/FastAPI; Java/Spring foi escolhido pela integração nativa dos componentes acima e pela tipagem estática. O principal custo é uma aplicação mais pesada que soluções baseadas em runtime JavaScript ou Python.

STOMP sobre WebSocket foi adotado para padronizar destinos, inscrições e mensagens. Socket.IO seria uma alternativa válida, mas adicionaria um protocolo específico que não é necessário neste momento. JWT mantém a API sem estado, em vez de sessões armazenadas no servidor.

## Redis

O projeto já declara `spring-boot-starter-data-redis`, pois Redis é necessário para presença e distribuição de eventos entre instâncias. A configuração e a integração efetiva com Redis ainda não foram implementadas; essa pendência será tratada na decisão específica de Redis.
