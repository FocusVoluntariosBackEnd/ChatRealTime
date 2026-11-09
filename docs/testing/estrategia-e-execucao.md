
# Estratégia e execução de testes

## Estratégia

O projeto utiliza testes unitários automatizados para validar regras de negócio e validação funcional manual pelos canais disponibilizados pela aplicação.

Os testes unitários cobrem as regras críticas: autenticação e autorização, criação de conversas privadas e grupos, associação de membros, envio idempotente de mensagens, paginação de histórico, confirmações de leitura, notificações e presença com múltiplas conexões.

Os fluxos HTTP são validados pelo Swagger. Um frontend de exemplo é utilizado para validar os fluxos completos, incluindo conexão WebSocket, envio e recebimento de eventos, reconexão, histórico, notificações e presença.

## Execução dos testes unitários

```bash
./mvnw test
```

No Windows:

```powershell
.\mvnw.cmd test
```

## Roteiro de validação funcional

1. Cadastre usuários e obtenha tokens pelo Swagger.
2. Crie conversa privada e grupo, incluindo e removendo membros conforme as permissões.
3. Envie mensagens pelo frontend de teste e confira o ACK, o evento de nova mensagem e o histórico HTTP.
4. Marque mensagens e notificações como lidas e confira as atualizações recebidas.
5. Abra mais de uma conexão para um usuário e valide as transições de presença.
6. Execute duas instâncias conectadas ao mesmo PostgreSQL e Redis e valide a distribuição dos eventos.
7. Desconecte e reconecte o cliente, recuperando mensagens e notificações persistidas pela API HTTP.

## Escopo

A estratégia não prevê testes automatizados de interface, end-to-end ou carga. Esses fluxos são verificados manualmente por meio do Swagger e do frontend de exemplo.
