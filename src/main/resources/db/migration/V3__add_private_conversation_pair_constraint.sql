ALTER TABLE conversations
ADD COLUMN user_a_id UUID,
ADD COLUMN user_b_id UUID,
ADD CONSTRAINT fk_conversations_user_a FOREIGN KEY (user_a_id) REFERENCES users(id),
ADD CONSTRAINT fk_conversations_user_b FOREIGN KEY (user_b_id) REFERENCES users(id);

ALTER TABLE conversations
ADD CONSTRAINT chk_conversations_type_users CHECK (
    (type = 'GROUP' AND user_a_id IS NULL AND user_b_id IS NULL)
    OR
    (type = 'PRIVATE' AND user_a_id IS NOT NULL AND user_b_id IS NOT NULL AND user_a_id < user_b_id)
);

CREATE UNIQUE INDEX uk_conversations_private_users
ON conversations (user_a_id, user_b_id)
WHERE type = 'PRIVATE';