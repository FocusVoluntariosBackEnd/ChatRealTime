ALTER TABLE messages
DROP CONSTRAINT IF EXISTS uk_client_message_id;

ALTER TABLE messages
    ADD CONSTRAINT uk_conversation_client_message_id UNIQUE (conversation_id, client_message_id);