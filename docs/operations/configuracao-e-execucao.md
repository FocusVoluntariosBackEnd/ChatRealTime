
# Configuração e execução

## Pré-requisitos

- Java 25;
- Docker e Docker Compose;
- Maven Wrapper do projeto.

## Variáveis de ambiente

Crie um arquivo `.env`, a partir de `.env.example`, com:

```properties
DB_NAME=chat
DB_USER=chat
DB_PASSWORD=chat
JWT_SECRET=<chave-Base64-com-pelo-menos-32-bytes>
REDIS_HOST=localhost
REDIS_PORT=6379
```

O arquivo `.env` contém valores locais e não deve ser versionado.

## Serviços de infraestrutura

Inicie PostgreSQL e Redis:

```bash
docker compose up -d
```

PostgreSQL fica disponível na porta `5432` e Redis na porta `6379`.

## Backend

Execute as migrations do Flyway e inicie a aplicação:

```bash
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

A aplicação usa PostgreSQL como banco persistente e Redis para presença e distribuição de eventos entre instâncias.
