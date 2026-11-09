
# Múltiplas instâncias

## Execução

A demonstração com duas instâncias usa o mesmo PostgreSQL e o mesmo Redis. Cada instância deve ser iniciada com configuração de banco e Redis compartilhada, em portas HTTP distintas.

```bash
./mvnw spring-boot:run --spring.web.server.port=8080
./mvnw spring-boot:run --spring.web.server.port=8081
```

## Distribuição

Redis Pub/Sub distribui eventos de mensagens, confirmações de leitura, notificações e presença para todas as instâncias. Cada instância entrega localmente os eventos aos clientes WebSocket que mantém conectados.

O estado temporário de presença é compartilhado no Redis. Assim, o estado do usuário considera conexões abertas em mais de uma instância.

O PostgreSQL permanece como fonte de verdade para conversas, mensagens, leituras e notificações.
