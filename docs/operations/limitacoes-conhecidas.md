
# Limitações conhecidas

Redis Pub/Sub não armazena eventos para consumo posterior. Portanto, um cliente desconectado pode não receber eventos em tempo real emitidos durante a desconexão.

A recuperação é feita consultando o histórico de mensagens e as notificações persistidas pela API HTTP após reconectar. O ACK de mensagem confirma a persistência, não a entrega ou a leitura pelo destinatário.
