# Teste pelo FrontTeste

O frontend em `frontTeste/index.html` é uma ferramenta manual para acompanhar os eventos WebSocket e a distribuição via Redis.

## Preparação

Inicie a infraestrutura e a aplicação:

```bash
docker compose up -d
./mvnw spring-boot:run
```

No Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

Abra o Swagger em `http://localhost:8080/swagger-ui/index.html`.

## Usuários e conversa

1. Crie dois usuários pelo Swagger.
2. Faça login com cada usuário e copie os respectivos `accessToken`s.
3. Com o token do primeiro usuário, crie uma conversa privada com o segundo usuário em `POST /api/conversations/private`.
4. Copie o ID da conversa criada.

## Conexão pelo frontend

1. Abra `frontTeste/index.html` em duas abas do navegador.
2. Em cada aba, informe `ws://localhost:8080/ws`, o mesmo ID da conversa e o JWT de um usuário diferente.
3. Clique em **Conectar** nas duas abas.
4. Confira o status de conexão e os eventos exibidos no painel **Eventos**.

## Mensagens, ACK e leitura

1. Na primeira aba, escreva uma mensagem e clique em **Enviar**.
2. Confira o evento `message:ack` para o remetente.
3. Confira o evento `message:new` e a mensagem exibida nas duas abas.
4. Na aba do destinatário, a mensagem recebida é marcada automaticamente como lida, pois a conversa está aberta.
5. Confira o evento `message:read` enviado pelo frontend e o evento `message:read:update` distribuído para a conversa.

## Notificações e presença

1. Ao receber uma mensagem de outro participante, confira o evento `notification:new` na aba do destinatário.
2. Conecte o segundo usuário com o primeiro já conectado e confira `user:presence` com status `ONLINE`.
3. Desconecte o segundo usuário e confira o evento `user:presence` com status `OFFLINE` para o primeiro participante.
4. Para testar múltiplas conexões, abra duas abas com o mesmo JWT. O usuário só deve ficar `OFFLINE` após a última conexão ser encerrada.

## Distribuição entre instâncias

Inicie uma segunda instância, usando o mesmo PostgreSQL e Redis:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

Depois, conecte uma aba em `ws://localhost:8080/ws` e outra em `ws://localhost:8081/ws`. Com usuários participantes da mesma conversa, repita os testes de mensagens, leituras, notificações e presença. Os eventos devem chegar aos clientes conectados nas duas instâncias.

## Recuperação após desconexão

O FrontTeste valida os eventos WebSocket e a distribuição Redis. Para conferir mensagens ou notificações persistidas durante uma desconexão, use o Swagger para consultar o histórico da conversa e as notificações do usuário.
