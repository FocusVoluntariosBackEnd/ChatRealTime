ALTER TABLE messages
ADD CONSTRAINT uk_client_message_id UNIQUE (client_message_id);